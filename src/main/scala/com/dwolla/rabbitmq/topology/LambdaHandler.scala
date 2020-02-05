package com.dwolla.rabbitmq.topology

import cats._
import cats.effect._
import cats.implicits._
import cats.tagless._
import cats.tagless.diagnosis._
import cats.tagless.implicits._
import com.amazonaws.services.lambda.runtime.Context
import com.dwolla.fs2aws.kms.KmsAlg
import com.dwolla.lambda._
import com.dwolla.rabbitmq.topology.WithTracingOps._
import com.dwolla.rabbitmq.topology.model._
import io.circe._
import io.circe.syntax._
import natchez._
import org.http4s.client.Client
import org.http4s.ember.client._

class LambdaHandler extends IOLambda[RabbitMQConfig, Unit] {
// TODO enable XRay Tracing
//  override val tracingEntryPoint: Resource[IO, EntryPoint[IO]] = AWSXRayTracer.entryPoint[IO]

  override def handleRequestF[F[_] : Concurrent : ContextShift : Timer : Trace](blocker: Blocker)
                                                                               (req: RabbitMQConfig, context: Context): F[LambdaResponse[Unit]] = {
    val resources: Resource[F, LambdaHandlerAlg[F]] =
      for {
        kms <- KmsAlg.resource[F].map(_.withTracing)
        http <- EmberClientBuilder.default[F].build
      } yield LambdaHandlerAlg(kms, http)

    resources.use { alg =>
      for {
        topology <- alg.fetchTopology(req)
        _ <- alg.printJson(topology)
      } yield ()
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
  def apply[F[_] : Concurrent : Trace](kmsAlg: KmsAlg[F], httpClient: Client[F]): LambdaHandlerAlg[F] = new LambdaHandlerAlg[F] {
    override def fetchTopology(input: RabbitMQConfig): F[RabbitMqTopology] =
      for {
        password <- kmsAlg.decrypt(input.password).map(tagPassword)
        topology <- RabbitMqTopologyAlg[F](httpClient, input.baseUri, input.username, password).retrieveTopology
      } yield topology

    override def printJson[A: Encoder](a: A): F[Unit] =
      Sync[F].delay(println(Printer.noSpaces.print(a.asJson)))
  }.withTracing
}

object WithTracingOps {
  class WithTracingSyntax[Alg[_[_]] : Instrument : FunctorK, F[_] : Trace](f: Alg[F]) {
    private def toTraceFunctionK: Instrumentation[F, *] ~> F = new (Instrumentation[F, *] ~> F) {
      override def apply[A](fa: Instrumentation[F, A]): F[A] = Trace[F].span(s"${fa.algebraName}.${fa.methodName}")(fa.value)
    }

    def withTracing: Alg[F] = {
      Instrument[Alg].instrument(f).mapK(toTraceFunctionK)
    }
  }

  implicit def toWithTracingSyntax[Alg[_[_]] : Instrument : FunctorK, F[_] : Trace](f: Alg[F]): WithTracingSyntax[Alg, F] =
    new WithTracingSyntax(f)
}
