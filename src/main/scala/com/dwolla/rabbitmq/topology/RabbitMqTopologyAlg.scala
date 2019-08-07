package com.dwolla.rabbitmq.topology

import cats.effect._
import cats.implicits._
import com.dwolla.rabbitmq.topology.model._
import io.circe._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.blaze._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.headers.Authorization

trait RabbitMqTopologyAlg[F[_]] {
  def retrieveTopology: F[RabbitMqTopology]
  def putTopology(rabbitMqTopology: RabbitMqTopology): F[Unit]
}

object RabbitMqTopologyAlg {
  // TODO re-implement using optics
  private def redactPasswordFields(jo: JsonObject): Json =
    Json.fromFields(jo.toList.map {
      case (k, v) if k contains "password" => (k, v.mapString(_ => "redacted"))
      case (k, v: Json) => (k, redactPasswordFields(v))
    })

  private def redactPasswordFields(j: Json): Json =
    j.fold(j, Json.fromBoolean, Json.fromJsonNumber, Json.fromString, a => Json.fromValues(a.map(redactPasswordFields)), redactPasswordFields)

  def resource[F[_] : ConcurrentEffect : Timer : ContextShift](baseUri: Uri): Resource[F, RabbitMqTopologyAlg[F]] =
    for {
      b <- Blocker[F]
      httpClient <- BlazeClientBuilder[F](b.blockingContext).resource.map(client.middleware.Logger[F](logHeaders = true, logBody = true))
    } yield new RabbitMqTopologyAlg[F] with Http4sClientDsl[F] {
      private val definitionsUri = baseUri / "api" / "definitions"

      override def retrieveTopology: F[RabbitMqTopology] =
        for {
          req <- GET(definitionsUri, Authorization(BasicCredentials("guest", "guest")))
          res <- httpClient.expect[Json](req).map(redactPasswordFields)
          topology <- res.as[RabbitMqTopology].liftTo[F]
        } yield topology

      override def putTopology(rabbitMqTopology: RabbitMqTopology): F[Unit] =
        for {
          req <- POST(rabbitMqTopology, definitionsUri, Authorization(BasicCredentials("guest", "guest")))
          res <- httpClient.expect[Unit](req)
        } yield res
    }
}
