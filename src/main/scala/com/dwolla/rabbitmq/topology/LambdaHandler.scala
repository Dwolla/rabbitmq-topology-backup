package com.dwolla.rabbitmq.topology

import cats.Functor
import cats.data.Kleisli
import cats.effect.std.Random
import cats.effect.{Trace => _, _}
import cats.syntax.all._
import cats.tagless.FunctorK.ops.toAllFunctorKOps
import com.dwolla.aws.kms.KmsAlg
import com.dwolla.rabbitmq.topology.LambdaHandler.nothingEncoder
import com.dwolla.rabbitmq.topology.model._
import com.dwolla.tracing._
import feral.lambda.natchez.{AwsTags, KernelSource}
//import feral.lambda.natchez.TracedLambda
import feral.lambda.{IOLambda, LambdaEnv}
import fs2.INothing
import io.circe.Encoder
import natchez._
import natchez.http4s.NatchezMiddleware
import org.http4s.client.{Client, middleware}
import org.http4s.ember.client._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.annotation.nowarn
import natchez.noop.NoopEntrypoint

class LambdaHandler extends IOLambda[RabbitMQConfig, INothing] {
  val lambdaName = "RabbitMQ-Topology-Backup"

  private implicit def kleisliLogger[F[_] : Logger, A]: Logger[Kleisli[F, A, *]] = Logger[F].mapK(Kleisli.liftK)

  private implicit def kleisliLambdaEnv[F[_] : Functor, A, B](implicit env: LambdaEnv[F, A]): LambdaEnv[Kleisli[F, B, *], A] =
    env.mapK(Kleisli.liftK)

  private def httpClient[F[_] : Async]: Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .build
      .map(middleware.Logger[F](logHeaders = true, logBody = false))

  private def resources[F[_] : Async] =
    Resource.eval(Random.scalaUtilRandom[F]).flatMap { _ =>
      (
        Resource.pure[F, EntryPoint[F]](NoopEntrypoint[F]()),
        KmsAlg.resource[F],
        httpClient[F],
        Resource.eval(Slf4jLogger.fromName[F](lambdaName))
      ).tupled
    }

  private def handlerF[F[_] : Async : Logger : LambdaEnv[*[_], RabbitMQConfig]](ep: EntryPoint[F], kms: KmsAlg[F], http: Client[F]): F[Option[INothing]] =
    TracedLambda(ep) { span =>
      LambdaHandler(
        kms.mapK(Kleisli.liftK[F, Span[F]]).withTracing,
        NatchezMiddleware.client(http.translate(Kleisli.liftK[F, Span[F]])(Kleisli.applyK(span)))
      ).run(span)
    }

  override def handler = resources[IO].map { case (ep, kms, http, logger) => implicit env => 
    implicit val l = logger
    handlerF(ep, kms, http)
  }
}

object LambdaHandler {
  @nowarn("msg=dead code following this construct")
  implicit val nothingEncoder: Encoder[INothing] = _ => ???

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

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all._
import feral.lambda.LambdaEnv
import natchez.EntryPoint
import natchez.Span

object TracedLambda {
  def apply[F[_] : MonadCancelThrow, Event, Result](entryPoint: EntryPoint[F])
                                                   (lambda: Span[F] => F[Option[Result]])
                                                   (implicit
                                                    env: LambdaEnv[F, Event],
                                                    KS: KernelSource[Event]): F[Option[Result]] =
    for {
      event <- env.event
      context <- env.context
      kernel = KernelSource[Event].extract(event)
      result <- entryPoint.continueOrElseRoot(context.functionName, kernel).use { span =>
        span.put(
          AwsTags.arn(context.invokedFunctionArn),
          AwsTags.requestId(context.awsRequestId)
        ) >> lambda(span)
      }
    } yield result
}
