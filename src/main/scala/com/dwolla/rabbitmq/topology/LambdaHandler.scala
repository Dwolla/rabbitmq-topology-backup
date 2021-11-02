package com.dwolla.rabbitmq.topology

import cats._
import cats.syntax.all._
import cats.effect.{Trace => _, _}
import cats.tagless._
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.WithTracingOps._
import com.dwolla.rabbitmq.topology.model._
import feral.lambda
import feral.lambda.IOLambda
import io.circe._
import io.circe.syntax._
import natchez._
import natchez.xray.{XRay, XRayEnvironment}
import org.http4s.client.Client
import org.http4s.ember.client._
import org.typelevel.log4cats.Logger
import cats.tagless.syntax.all._
import cats.tagless.aop._
import natchez.Trace.ioTrace
import natchez.http4s.NatchezMiddleware
import org.typelevel.log4cats.slf4j.Slf4jLogger

class LambdaHandler extends IOLambda[RabbitMQConfig, Unit] {
  override type Setup = (KmsAlg[IO], Client[IO])

  val lambdaName = "RabbitMQ-Topology-Backup"

  private def traceResource: Resource[IO, Trace[IO]] =
    Resource.eval(XRayEnvironment[IO].daemonAddress)
      .flatMap {
        case Some(addr) => XRay.entryPoint[IO](addr)
        case None => XRay.entryPoint[IO]()
      }
      .evalMap(XRayEnvironment[IO].kernelFromEnvironment.tupleLeft(_))
      .flatMap { case (ep, kernel) => ep.continueOrElseRoot(lambdaName, kernel) }
      .evalMap(ioTrace)

  override protected def setup: Resource[IO, Setup] =
    for {
      kms <- KmsAlg.resource[IO]
      http <- EmberClientBuilder.default[IO].build
    } yield (kms, http)

  override def apply(event: RabbitMQConfig, context: lambda.Context, setup: Setup): IO[Option[Unit]] =
    Slf4jLogger.fromName[IO](lambdaName).flatMap { implicit logger =>
      traceResource.use { implicit trace =>
        val alg = LambdaHandlerAlg[IO](setup._1.withTracing, NatchezMiddleware.client(setup._2))

        for {
          topology <- alg.fetchTopology(event)
          _ <- alg.printJson(topology)
        } yield None
      }
    }
}

@autoInstrument
@autoFunctorK
trait LambdaHandlerAlg[F[_]] {
  def fetchTopology(input: RabbitMQConfig): F[RabbitMqTopology]
  def printJson[A: Encoder](a: A): F[Unit]
}

object LambdaHandlerAlg {
  def apply[F[_] : Async : Logger : Trace](kmsAlg: KmsAlg[F], httpClient: Client[F]): LambdaHandlerAlg[F] = new LambdaHandlerAlg[F] {
    override def fetchTopology(input: RabbitMQConfig): F[RabbitMqTopology] =
      for {
        password <- kmsAlg.decrypt(input.password).map(tagPassword)
        topology <- RabbitMqTopologyAlg[F](httpClient, input.baseUri, input.username, password).retrieveTopology
      } yield topology

    override def printJson[A: Encoder](a: A): F[Unit] =
      Logger[F].info(Printer.noSpaces.print(a.asJson))
  }.withTracing
}

object WithTracingOps {
  class WithTracingSyntax[Alg[_[_]] : Instrument, F[_] : Trace](f: Alg[F]) {
    private def toTraceFunctionK: Instrumentation[F, *] ~> F = new (Instrumentation[F, *] ~> F) {
      override def apply[A](fa: Instrumentation[F, A]): F[A] = Trace[F].span(s"${fa.algebraName}.${fa.methodName}")(fa.value)
    }

    def withTracing: Alg[F] =
      Instrument[Alg].instrument(f).mapK(toTraceFunctionK)
  }

  implicit def toWithTracingSyntax[Alg[_[_]] : Instrument, F[_] : Trace](f: Alg[F]): WithTracingSyntax[Alg, F] =
    new WithTracingSyntax(f)
}
