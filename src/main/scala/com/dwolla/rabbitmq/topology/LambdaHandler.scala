package com.dwolla.rabbitmq.topology

import cats.effect._
import com.dwolla.fs2aws.kms.KmsAlg
import com.dwolla.lambda.IOLambda
import com.dwolla.rabbitmq.topology.model._
import io.circe.Printer

class LambdaHandler(printer: Printer) extends IOLambda[RabbitMQConfig, RabbitMqTopology](printer) {
  def this() = this(Printer.noSpaces)

  override def handleRequest(blocker: Blocker)
                            (input: RabbitMQConfig): IO[Option[RabbitMqTopology]] =
    for {
      password <- KmsAlg.resource[IO].use(_.decrypt(input.password)).map(tagPassword)
      topology <- RabbitMqTopologyAlg.resource[IO](blocker, input.hostname, input.username, password).use(_.retrieveTopology)
    } yield Option(topology)
}
