package com.dwolla.rabbitmq.topology

import cats.Monad
import cats.data.Kleisli
import cats.effect.{Trace => _, _}
import cats.syntax.all._
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.model._
import feral.lambda
import feral.lambda.tracing.XRayTracedLambda
import feral.lambda.{Context, IOLambda}
import feral.lambda.IOLambda._
import natchez._
import natchez.http4s.NatchezMiddleware
import org.http4s.client.middleware
import org.http4s.ember.client._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.dwolla.tracing._
import fs2.INothing

class LambdaHandler extends IOLambda[RabbitMQConfig, INothing] {
  val lambdaName = "RabbitMQ-Topology-Backup"

  def handler[F[_] : Async : Trace]: Resource[F, lambda.Lambda[F, RabbitMQConfig, INothing]] =
    KmsAlg
      .resource[F]
      .map(_.withTracing)
      .flatMap { kms =>
        EmberClientBuilder
          .default[F]
          .build
          .map(middleware.Logger[F](logHeaders = true, logBody = false))
          .map(NatchezMiddleware.client(_))
          .evalMap { http =>
            Slf4jLogger.fromName[F](lambdaName)
              .map(implicit logger => LambdaHandlerAlg(kms, http))
          }
          .map(LambdaHandler(_))
      }

  override def run: Resource[IO, lambda.Lambda[IO, RabbitMQConfig, INothing]] =
    XRayTracedLambda(handler[Kleisli[IO, Span[IO], *]])

}

object LambdaHandler {
  def apply[F[_] : Monad](alg: LambdaHandlerAlg[F]): lambda.Lambda[F, RabbitMQConfig, INothing] =
    (event: RabbitMQConfig, _: Context[F]) =>
      for {
        topology <- alg.fetchTopology(event)
        _ <- alg.printJson(topology)
      } yield none
}
