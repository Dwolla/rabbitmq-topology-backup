package com.dwolla.rabbitmq.topology

import cats.effect._
import com.dwolla.lambda.IOLambda
import com.dwolla.rabbitmq.topology.model._
import io.circe.Printer
import org.http4s._

class LambdaHandler(printer: Printer) extends IOLambda[RabbitMQConfig, RabbitMqTopology](printer) {
  def this() = this(Printer.noSpaces)

  protected def rabbitmqResource(uri: Uri, username: Username, password: Password): Resource[IO, RabbitMqTopologyAlg[IO]] =
    RabbitMqTopologyAlg.resource[IO](uri, username, password)

  override def handleRequest(input: RabbitMQConfig): IO[Option[RabbitMqTopology]] = {
    for {
      password <- KmsAlg.resource[IO].use(_.decrypt(input.password)).map(tagPassword)
      topology <- rabbitmqResource(input.hostname, input.username, password).use(_.retrieveTopology)
    } yield Option(topology)
  }
}
