package com.dwolla.lambda

import java.io._

import _root_.fs2.io.readInputStream
import _root_.fs2.text.utf8Decode
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.amazonaws.services.lambda.runtime._
import com.dwolla.lambda.CatsLambda._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import IOLambda._

import scala.concurrent.ExecutionContext

abstract class IOLambda[A: Decoder, B: Encoder](printer: Printer = Defaults.printer,
                                                logRequest: Boolean = Defaults.logRequest,
                                                executionContext: ExecutionContext = Defaults.executionContext) extends RequestStreamHandler {
  protected implicit def contextShift: ContextShift[IO] = cats.effect.IO.contextShift(executionContext)
  protected implicit def timer: Timer[IO] = cats.effect.IO.timer(executionContext)

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit =
    Blocker[IO].use { blocker =>
      IOLambda(blocker, this, printer, logRequest).handleRequestAndWriteResponse(input, output)
    }.unsafeRunSync()

  def handleRequest(blocker: Blocker)(a: A): IO[Option[B]]
}

object IOLambda {
  object Defaults {
    val printer: Printer = Printer.noSpaces
    val executionContext: ExecutionContext = ExecutionContext.global
    val logRequest: Boolean = true
  }

  def apply[A: Decoder, B: Encoder](blocker: Blocker,
                                    ioLambda: IOLambda[A, B],
                                    printer: Printer,
                                    logRequest: Boolean)
                                   (implicit CS: ContextShift[IO]): LambdaF[IO, A, B] =
    new LambdaF[IO, A, B](blocker, printer, logRequest) {
      override def handleRequest(req: A): IO[Option[B]] = ioLambda.handleRequest(blocker)(req)
    }
}

abstract class LambdaF[F[_] : Sync : ContextShift, A: Decoder, B: Encoder](blocker: Blocker,
                                                                           printer: Printer = Printer.noSpaces,
                                                                           logRequest: Boolean = true) {
  def handleRequest(req: A): F[Option[B]]

  protected implicit def logger: Logger[F] = Slf4jLogger.getLoggerFromName[F]("LambdaLogger")

  private val logRequestF: F[Boolean] = logRequest.pure[F]

  private def readFrom(inputStream: InputStream): F[String] =
    readInputStream(Sync[F].delay(inputStream), 4096, blocker)
      .through(utf8Decode[F])
      .compile
      .lastOrError

  private def printTo(outputStream: OutputStream)(b: B): F[Unit] =
    Resource
      .fromAutoCloseable(Sync[F].delay(new PrintStream(outputStream)))
      .use { ps =>
        Sync[F].delay(ps.print(printer.pretty(b.asJson)))
      }

  private def writeTo(outputStream: OutputStream)(maybeB: Option[B]): F[Unit] =
    OptionT.fromOption[F](maybeB)
      .semiflatMap(printTo(outputStream))
      .value
      .void

  private def parseStringLoggingErrors(str: String): F[Json] =
    parse(str)
      .toEitherT[F]
      .leftSemiflatTap(Logger[F].error(_)(s"Could not parse the following input:\n$str"))
      .leftWiden[Throwable].rethrowT
      .flatTap(logJsonIfEnabled)

  private def logJsonIfEnabled(json: Json): F[Unit] =
    logRequestF.ifA(Logger[F].info(
      s"""Received input:
         |${printer.pretty(json)}""".stripMargin), Applicative[F].unit)

  private def parseStream(inputStream: InputStream): F[A] =
    for {
      str <- readFrom(inputStream)
      json <- parseStringLoggingErrors(str)
      req <- json.as[A].liftTo[F]
    } yield req

  def handleRequestAndWriteResponse(inputStream: InputStream, outputStream: OutputStream): F[Unit] =
    parseStream(inputStream) >>= handleRequest >>= writeTo(outputStream)

}

object CatsLambda {
  implicit class LeftSemiflatTap[F[_] : Monad, A, B](eitherT: EitherT[F, A, B]) {
    def leftSemiflatTap[C](f: A => F[C]): EitherT[F, A, B] =
      eitherT.leftSemiflatMap(x => f(x) map (_ => x))
  }
}
