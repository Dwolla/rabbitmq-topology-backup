package com.dwolla.rabbitmq.topology

import cats.effect._
import com.dwolla.lambda.IOLambda
import com.dwolla.rabbitmq.topology.model.RabbitMqTopology
import io.circe.Printer
import org.http4s._

class Lambda(printer: Printer) extends IOLambda[Unit, RabbitMqTopology](printer) {
  def this() = this(Printer.noSpaces)

  lazy val rabbitmqResource: Resource[IO, RabbitMqTopologyAlg[IO]] = RabbitMqTopologyAlg.resource[IO](uri"https://rabbit.us-west-2.devint.dwolla.net")

  override def handleRequest(input: Unit): IO[Option[RabbitMqTopology]] =
    rabbitmqResource.use(_.retrieveTopology.map(Option.apply))
}
