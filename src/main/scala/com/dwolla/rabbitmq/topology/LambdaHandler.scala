package com.dwolla.rabbitmq.topology

import cats.effect._
import cats.implicits._
import com.dwolla.fs2aws.kms.KmsAlg
import com.dwolla.lambda.IOLambda
import com.dwolla.rabbitmq.topology.LambdaHandler._
import com.dwolla.rabbitmq.topology.model._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe._
import io.circe.syntax._

class LambdaHandler(printer: Printer) extends IOLambda[RabbitMQConfig, Unit](printer) {
  def this() = this(Printer.noSpaces)

  private implicit val p: Printer = printer
  private implicit val logger: Logger[IO] = Slf4jLogger.getLoggerFromName[IO]("LambdaLogger")

  override def handleRequest(blocker: Blocker)
                            (input: RabbitMQConfig): IO[Option[Unit]] =
    fetchTopology[IO](blocker)(input)
      .flatMap(printJson[IO](_))
      .map(Option(_))
}

object LambdaHandler {
  def fetchTopology[F[_] : ConcurrentEffect](blocker: Blocker)(input: RabbitMQConfig): F[RabbitMqTopology] =
    for {
      password <- KmsAlg.resource[F].use(_.decrypt(input.password)).map(tagPassword)
      topology <- RabbitMqTopologyAlg.resource[F](blocker, input.baseUri, input.username, password).use(_.retrieveTopology)
    } yield topology

  def printJson[F[_]] = new PartiallyAppliedPrintJson[F]

  class PartiallyAppliedPrintJson[F[_]] {
    def apply[A : Encoder](a: A)(implicit logger: Logger[F], printer: Printer): F[Unit] =
      logger.info(printer.print(a.asJson))
  }
}
