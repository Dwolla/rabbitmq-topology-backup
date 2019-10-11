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
import org.http4s.client.Client
import org.http4s.headers.Authorization

trait RabbitMqTopologyAlg[F[_]] {
  def retrieveTopology: F[RabbitMqTopology]
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

  def resource[F[_] : ConcurrentEffect](blocker: Blocker, baseUri: Uri, username: Username, password: Password): Resource[F, RabbitMqTopologyAlg[F]] =
    BlazeClientBuilder[F](blocker.blockingContext)
      .resource
      .map(client.middleware.Logger[F](logHeaders = true, logBody = false))
      .map(new RabbitMqTopologyAlgImpl[F](_, baseUri, username, password))

  private[topology] class RabbitMqTopologyAlgImpl[F[_] : Sync](httpClient: Client[F],
                                                               baseUri: Uri,
                                                               username: Username,
                                                               password: Password) extends RabbitMqTopologyAlg[F] with Http4sClientDsl[F] {
    private val definitionsUri = baseUri / "api" / "definitions"
    private val authorizationHeader = Authorization(BasicCredentials(username, password))

    override def retrieveTopology: F[RabbitMqTopology] =
      for {
        req <- GET(definitionsUri, authorizationHeader)
        res <- httpClient.expect[Json](req).map(redactPasswordFields)
        topology <- res.as[RabbitMqTopology].liftTo[F]
      } yield topology
  }

}
