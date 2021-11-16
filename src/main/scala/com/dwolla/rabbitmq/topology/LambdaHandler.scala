package com.dwolla.rabbitmq.topology

import cats.data._
import cats.effect.std.Random
import cats.effect.{Trace => _, _}
import cats.syntax.all._
import com.dwolla.fs2aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.model._
import com.dwolla.tracing._
import feral.lambda._
import natchez._
import natchez.xray.XRay
import org.http4s.client.{Client, middleware}
import org.http4s.ember.client._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class LambdaHandler extends IOLambda[RabbitMQConfig, INothing] {
  private implicit def kleisliLogger[F[_] : Logger, A]: Logger[Kleisli[F, A, *]] = Logger[F].mapK(Kleisli.liftK)

  private implicit def xrayKernelSource[Event]: KernelSource[Event] = KernelSource.emptyKernelSource

  private def httpClient[F[_] : Async]: Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .build
      .map(middleware.Logger[F](logHeaders = true, logBody = false))

  def handlerF[F[_] : Async]: Resource[F, LambdaEnv[F, RabbitMQConfig] => F[Option[INothing]]] =
    for {
      implicit0(logger: Logger[F]) <- Resource.eval(Slf4jLogger.fromName[F]("RabbitMQ-Topology-Backup"))
      implicit0(random: Random[F]) <- Resource.eval(Random.scalaUtilRandom[F])
      ep <- XRay.entryPoint[F]()
      kms <- KmsAlg.resource[F]
      http <- httpClient[F]
    } yield { implicit env =>
      TracedHandler(ep, Kleisli { (span: Span[F]) =>
        LambdaHandler(kms.withTracing, http.tracedWith(span)).run(span)
      })
    }

  override def handler: Resource[IO, LambdaEnv[IO, RabbitMQConfig] => IO[Option[INothing]]] =
    handlerF[IO]
}

object LambdaHandler {
  def apply[F[_] : Concurrent : Logger : Trace](kms: KmsAlg[F],
                                                http: Client[F])
                                               (implicit env: LambdaEnv[F, RabbitMQConfig]): F[Option[INothing]] =
    for {
      event <- env.event
      alg = LambdaHandlerAlg(kms, http)
      topology <- alg.fetchTopology(event)
      _ <- alg.printJson(topology)
    } yield none
}
