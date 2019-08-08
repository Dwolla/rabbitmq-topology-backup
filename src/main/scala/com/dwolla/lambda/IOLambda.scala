package com.dwolla.lambda

import java.io._

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.amazonaws.services.lambda.runtime._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.logging.log4j._

import scala.concurrent.ExecutionContext
import scala.io.Source

abstract class IOLambda[A: Decoder, B: Encoder](printer: Printer, protected val executionContext: ExecutionContext) extends RequestStreamHandler { ioLambda =>
  def this(printer: Printer) = this(printer, ExecutionContext.global)
  def this() = this(Printer.noSpaces, ExecutionContext.global)

  protected implicit def contextShift: ContextShift[IO] = cats.effect.IO.contextShift(executionContext)
  protected implicit def timer: Timer[IO] = cats.effect.IO.timer(executionContext)

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit =
    new CatsLambda[IO, A, B] {
      override def handleRequest(req: A): IO[Option[B]] = ioLambda.handleRequest(req)
    }.handleRequestAndWriteResponse(input, output).unsafeRunSync()

  def handleRequest(t: A): IO[Option[B]]
}

abstract class CatsLambda[F[_] : Sync, A: Decoder, B: Encoder](printer: Printer = Printer.noSpaces, logRequest: Boolean = true) {
  def handleRequest(req: A): F[Option[B]]

  protected lazy val logger: Logger = LogManager.getLogger("LambdaLogger")
  private val logRequestF: F[Boolean] = logRequest.pure[F]

  private def readFrom(inputStream: InputStream): F[String] =
    Sync[F].delay(Source.fromInputStream(inputStream).mkString)

  private def writeTo(outputStream: OutputStream)(maybeB: Option[B]): F[Unit] = {
    // TODO the PrintStream should maybe come from a resource?
    def print(b: B): F[Unit] = Sync[F].delay(new PrintStream(outputStream).print(printer.pretty(b.asJson)))

    OptionT.fromOption[F](maybeB)
      .semiflatMap(print)
      .value
      .void
  }

  private def parseStringLoggingErrors(str: String): F[Json] =
    parse(str).toEitherT[F].leftSemiflatMap(ex => Sync[F].delay {
      logger.error(s"Could not parse the following input:\n$str", ex)
      ex
    }).leftWiden[Throwable].rethrowT
      .flatTap(logJsonIfEnabled)

  private def logJsonIfEnabled(json: Json): F[Unit] =
    logRequestF.ifA(Sync[F].delay(logger.info(
      """Received input:
        |{}""".stripMargin, json)), Applicative[F].unit)

  private def parseStream(inputStream: InputStream): F[A] =
    for {
      str <- readFrom(inputStream)
      json <- parseStringLoggingErrors(str)
      req <- json.as[A].liftTo[F]
    } yield req

  def handleRequestAndWriteResponse(inputStream: InputStream, outputStream: OutputStream): F[Unit] =
    parseStream(inputStream) >>= handleRequest >>= writeTo(outputStream)

}
