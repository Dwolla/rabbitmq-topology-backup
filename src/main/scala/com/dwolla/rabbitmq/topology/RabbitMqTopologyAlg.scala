package com.dwolla.rabbitmq.topology

import cats.effect._
import cats.implicits._
import cats.tagless.{autoFunctorK, autoInstrument}
import com.dwolla.rabbitmq.topology.model._
import io.circe._
import org.http4s._
import org.http4s.client.middleware
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import com.dwolla.rabbitmq.topology.WithTracingOps._
import natchez._

@autoInstrument
@autoFunctorK
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

  def apply[F[_] : Concurrent : Trace](client: Client[F], baseUri: Uri, username: Username, password: Password): RabbitMqTopologyAlg[F] =
      (new RabbitMqTopologyAlgImpl[F](middleware.Logger[F](logHeaders = true, logBody = false)(client), baseUri, username, password): RabbitMqTopologyAlg[F]).withTracing

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
