package com.dwolla.rabbitmq.topology

import cats.effect.{Trace => _, _}
import cats.syntax.all._
import cats.tagless._
import cats.tagless.aop.Instrument
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.model._
import com.dwolla.tracing._
import io.circe._
import io.circe.syntax._
import natchez._
import org.http4s.client.Client
import org.typelevel.log4cats.Logger

trait LambdaHandlerAlg[F[_]] {
  def fetchTopology(input: RabbitMQConfig): F[RabbitMqTopology]
  def printJson[A: Encoder](a: A): F[Unit]
}

object LambdaHandlerAlg {
  implicit val LambdaHandlerAlgInstrument: Instrument[LambdaHandlerAlg] = Derive.instrument
  implicit val LambdaHandlerAlgFunctorK: FunctorK[LambdaHandlerAlg] = Derive.functorK

  def apply[F[_] : Async : Logger : Trace](kmsAlg: KmsAlg[F], httpClient: Client[F]): LambdaHandlerAlg[F] =
    new LambdaHandlerAlg[F] {
      override def fetchTopology(input: RabbitMQConfig): F[RabbitMqTopology] =
        for {
          password <- kmsAlg.decrypt(input.password).map(tagPassword)
          topology <- RabbitMqTopologyAlg[F](httpClient, input.baseUri, input.username, password).retrieveTopology
        } yield topology

      override def printJson[A: Encoder](a: A): F[Unit] =
        Logger[F].info(Printer.noSpaces.print(a.asJson))
    }.withTracing
}
