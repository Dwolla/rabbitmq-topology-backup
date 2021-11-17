package com.dwolla.rabbitmq.topology

import cats.data.Kleisli
import cats.effect.{Trace => _, _}
import cats.syntax.all._
import cats.tagless.syntax.all._
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.model._
import feral.lambda
import feral.lambda.tracing.{LiftTrace, XRayTracedLambda}
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

  def handler[F[_] : Async, G[_] : Async : Trace](implicit LT: LiftTrace[F, G]): Resource[F, lambda.Lambda[G, RabbitMQConfig, INothing]] =
    KmsAlg
      .resource[F]
      .map(_.mapK(LT.liftK).withTracing)
      .flatMap { kms =>
        EmberClientBuilder
          .default[F]
          .build
          .map(_.translate(LT.liftK)(LT.lowerK))
          .map(middleware.Logger[G](logHeaders = true, logBody = false))
          .map(NatchezMiddleware.client(_))
          .evalMap { http =>
            Slf4jLogger.fromName[F](lambdaName)
              .map(_.mapK(LT.liftK))
              .map { implicit logger =>
                val alg: LambdaHandlerAlg[G] = LambdaHandlerAlg(kms, http)

                (event: RabbitMQConfig, _: Context[G]) =>
                  for {
                    topology <- alg.fetchTopology(event)
                    _ <- alg.printJson(topology)
                  } yield none
            }
          }
      }

  override def run: Resource[IO, lambda.Lambda[IO, RabbitMQConfig, INothing]] =
    handler[IO, Kleisli[IO, Span[IO], *]].flatMap(XRayTracedLambda.usingEnvironment(_))

}
