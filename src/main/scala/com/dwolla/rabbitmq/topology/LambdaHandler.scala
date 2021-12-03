package com.dwolla.rabbitmq.topology

import cats.effect.{Trace => _, _}
import cats.syntax.all._
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

  private def httpClient[F[_] : Async]: Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .build
      .map(middleware.Logger[F](logHeaders = true, logBody = false))

  override def handler = for {
    ep <- Resource.pure(NoopEntrypoint[IO]())
    kms <- KmsAlg.resource[IO]
    http <- httpClient[IO]
    logger <- Resource.eval(Slf4jLogger.fromName[IO](lambdaName))
  } yield { implicit env =>
    implicit val l = logger

    TracedLambda(ep) { span =>
      Trace.ioTrace(span).flatMap { implicit trace =>
        LambdaHandler(kms.withTracing, NatchezMiddleware.client(http))
      }
    }
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
