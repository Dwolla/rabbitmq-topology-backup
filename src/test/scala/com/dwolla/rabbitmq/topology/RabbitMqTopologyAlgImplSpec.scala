package com.dwolla.rabbitmq.topology

import cats.data._
import cats.effect._
import com.dwolla.rabbitmq.topology.RabbitMqTopologyAlg.RabbitMqTopologyAlgImpl
import com.dwolla.rabbitmq.topology.model.{Password, Username}
import com.eed3si9n.expecty.Expecty.expect
import io.circe.Json
import io.circe.literal._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.VirtualHost
import org.http4s.server.middleware.VirtualHost.exact
import org.http4s.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

class RabbitMqTopologyAlgImplSpec extends CatsEffectSuite with Http4sDsl[IO] {
  import FakeRabbitMqService._

  implicit def logger: Logger[IO] = NoOpLogger[IO]

  def workingAlg(resp: Json): RabbitMqTopologyAlg[IO] =
    new RabbitMqTopologyAlgImpl[IO](client(okDefinitions(resp)), correctBaseUri, correctUsername, correctPassword)

  test("RabbitMqTopologyAlgImpl should fetch the definitions API and return the results as a JSON object") {
    val resp = json"""{}"""

    val output = workingAlg(resp)
      .retrieveTopology

    output.map(x => expect(x == resp))
  }

  test("RabbitMqTopologyAlgImpl should redact any password fields") {
    val resp =
      json"""
          {
            "other": "safe!",
            "password": "string",
            "obj": {
              "password": "redacted",
              "probably-a-password": "yes it is!"
            },
            "list": [
              {
                "password": "yup"
              },
              {
                "password": "yup",
                "not": "nope",
                "boolean": true
              }
            ]
          }
          """
    val expected =
      json"""
          {
            "other": "safe!",
            "password": "redacted",
            "obj": {
              "password": "redacted",
              "probably-a-password": "redacted"
            },
            "list": [
              {
                "password": "redacted"
              },
              {
                "password": "redacted",
                "not": "nope",
                "boolean": true
              }
            ]
          }
          """

    val output = workingAlg(resp)
      .retrieveTopology

    output.map(x => expect(x == expected))
  }

}

object FakeRabbitMqService extends Http4sDsl[IO] {

  val correctUsername: Username = "guest".asInstanceOf[Username]
  val correctPassword: Password = "guest".asInstanceOf[Password]
  val correctBaseUri: Uri = uri"https://rabbitmq.us-west-2.devint.dwolla.net"
  val correctHostname: String = correctBaseUri.host.get.toString()

  def okDefinitions(resp: Json): AuthedRoutes[String, IO] = AuthedRoutes.of[String, IO] {
    case GET -> Root / "api" / "definitions" as _ =>
      Ok(resp)
  }

  private val authUser: Kleisli[OptionT[IO, *], Request[IO], String] =
    Kleisli { req: Request[IO] =>
      for {
        Authorization(BasicCredentials(user, pass)) <- req.headers.get[Authorization]
        if user == correctUsername && pass == correctPassword
      } yield correctUsername
    }.mapF(OptionT.fromOption[IO](_))

  private val middleware: AuthMiddleware[IO, String] =
    AuthMiddleware(authUser)

  def client(service: AuthedRoutes[String, IO]): Client[IO] =
    Client.fromHttpApp[IO](VirtualHost(exact(middleware(service).orNotFound, correctHostname)))

}
