package com.dwolla.rabbitmq.topology

import cats._
import cats.data._
import cats.effect.{Trace => _, _}
import cats.implicits._
import cats.tagless._
import com.comcast.ip4s._
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.WithTracingOps._
import com.dwolla.rabbitmq.topology.model._
import feral.lambda
import feral.lambda.IOLambda
import io.circe._
import io.circe.syntax._
import natchez._
import natchez.xray.XRay
import org.http4s.client.Client
import org.http4s.ember.client._
import org.typelevel.log4cats.Logger
import cats.tagless.syntax.all._
import cats.tagless.aop._
import natchez.Trace.ioTrace
import natchez.http4s.NatchezMiddleware
import org.typelevel.log4cats.slf4j.Slf4jLogger

class LambdaHandler extends IOLambda[RabbitMQConfig, Unit] {
  override type Setup = (Trace[IO], Logger[IO], LambdaHandlerAlg[IO])

  private def getXRayDaemonAddress[F[_] : Sync : Logger]: F[Option[SocketAddress[IpAddress]]] =
    OptionT(Sync[F].delay(sys.props.get("AWS_XRAY_DAEMON_ADDRESS")))
      .subflatMap(SocketAddress.fromStringIp)
      .value
      .flatTap(daemonAddress => Logger[F].info(s"Found X-Ray Daemon Address: $daemonAddress"))

  val lambdaName = "RabbitMQ-Topology-Backup"

  private def traceResource(implicit logger: Logger[IO]): Resource[IO, Trace[IO]] = {
    Resource.eval(getXRayDaemonAddress[IO])
      .flatMap {
        case Some(addr) => XRay.entryPoint[IO](addr)
        case None => XRay.entryPoint[IO]()
      }
      .flatMap(_.root(lambdaName))
      .evalMap(ioTrace)
  }

  override protected def setup: Resource[IO, Setup] =
    Resource.eval(Slf4jLogger.fromName[IO](lambdaName)).flatMap { implicit logger =>
      traceResource.flatMap { implicit trace =>
        for {
          kms <- KmsAlg.resource[IO]
          http <- EmberClientBuilder.default[IO].build
        } yield (trace, logger, LambdaHandlerAlg(kms.withTracing, NatchezMiddleware.client(http)))
      }
    }

  override def apply(event: RabbitMQConfig, context: lambda.Context, setup: Setup): IO[Option[Unit]] = {
    implicit val (trace@_, logger@_, _) = setup
    val alg = setup._3

    for {
      topology <- alg.fetchTopology(event)
      _ <- alg.printJson(topology)
    } yield None
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
//  class WithTracingSyntax[Alg[_[_]], F[_]](f: Alg[F])
//                                          (implicit AI: Instrument[Alg],
//                                           AFK: FunctorK[Alg],
//                                           FT: Trace[F]) {
    private def toTraceFunctionK: Instrumentation[F, *] ~> F = new (Instrumentation[F, *] ~> F) {
      override def apply[A](fa: Instrumentation[F, A]): F[A] = Trace[F].span(s"${fa.algebraName}.${fa.methodName}")(fa.value)
    }

    def withTracing: Alg[F] =
      Instrument[Alg].instrument(f).mapK(toTraceFunctionK)
  }

  implicit def toWithTracingSyntax[Alg[_[_]] : Instrument, F[_] : Trace](f: Alg[F]): WithTracingSyntax[Alg, F] =
//  implicit def toWithTracingSyntax[Alg[_[_]], F[_]](f: Alg[F])
//                                                   (implicit AI: Instrument[Alg],
//                                                    AFK: FunctorK[Alg],
//                                                    FT: Trace[F]): WithTracingSyntax[Alg, F] =
    new WithTracingSyntax(f)
}
