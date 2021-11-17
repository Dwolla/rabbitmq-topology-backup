package com.dwolla.rabbitmq.topology

import cats.effect.{Trace => _, _}
import cats.implicits._
import cats.tagless.aop.Instrument
import cats.tagless._
import com.dwolla.rabbitmq.topology.model._
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.client.Client
import org.http4s.headers.{Authorization, Connection}
import natchez._
import com.dwolla.tracing._

trait RabbitMqTopologyAlg[F[_]] {
  def retrieveTopology: F[RabbitMqTopology]
}

object RabbitMqTopologyAlg {
  implicit val RabbitMqTopologyAlgInstrument: Instrument[RabbitMqTopologyAlg] = Derive.instrument
  implicit val RabbitMqTopologyAlgFunctorK: FunctorK[RabbitMqTopologyAlg] = Derive.functorK

  // TODO re-implement using optics
  private def redactPasswordFields(jo: JsonObject): Json =
    Json.fromFields(jo.toList.map {
      case (k, v) if k contains "password" => (k, v.mapString(_ => "redacted"))
      case (k, v: Json) => (k, redactPasswordFields(v))
    })

  private def redactPasswordFields(j: Json): Json =
    j.fold(j, Json.fromBoolean, Json.fromJsonNumber, Json.fromString, a => Json.fromValues(a.map(redactPasswordFields)), redactPasswordFields)

  def apply[F[_] : Async : Trace](client: Client[F],
                                  baseUri: Uri,
                                  username: Username,
                                  password: Password): RabbitMqTopologyAlg[F] =
    (new RabbitMqTopologyAlgImpl[F](client, baseUri, username, password): RabbitMqTopologyAlg[F]).withTracing

  private[topology] class RabbitMqTopologyAlgImpl[F[_] : Concurrent](httpClient: Client[F],
                                                                     baseUri: Uri,
                                                                     username: Username,
                                                                     password: Password) extends RabbitMqTopologyAlg[F] with Http4sClientDsl[F] {
    private val definitionsUri = baseUri / "api" / "definitions"
    private val authorizationHeader = Authorization(BasicCredentials(username, password))

    override def retrieveTopology: F[RabbitMqTopology] =
      for {
        connectionCloseHeader <- Connection.parse("close").liftTo[F]
        req = GET(definitionsUri, authorizationHeader, connectionCloseHeader)
        res <- httpClient.expect[Json](req).map(redactPasswordFields)
        topology <- res.as[RabbitMqTopology].liftTo[F]
      } yield topology
  }
}
