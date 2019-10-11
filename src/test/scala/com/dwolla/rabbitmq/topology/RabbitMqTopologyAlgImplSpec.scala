package com.dwolla.rabbitmq.topology

import cats.Applicative
import cats.data._
import cats.effect._
import com.dwolla.rabbitmq.topology.RabbitMqTopologyAlg.RabbitMqTopologyAlgImpl
import com.dwolla.rabbitmq.topology.model.{Password, Username}
import com.dwolla.testutils.IOSpec
import io.chrisdavenport.log4cats.Logger
import io.circe.Json
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.VirtualHost
import org.http4s.server.middleware.VirtualHost.exact
import org.http4s.syntax.all._
import org.scalatest._

class RabbitMqTopologyAlgImplSpec extends IOSpec with Matchers with Http4sDsl[IO] with ParallelTestExecution {
  import FakeRabbitMqService._

  implicit def logger: Logger[IO] = NoOpLogger[IO]

  def workingAlg(resp: Json): RabbitMqTopologyAlg[IO] =
    new RabbitMqTopologyAlgImpl[IO](client(okDefinitions(resp)), correctBaseUri, correctUsername, correctPassword)

  behavior of "RabbitMqTopologyAlgImpl"

  it should "fetch the definitions API and return the results as a JSON object" inIO {
    val resp = json"""{}"""

    val output = workingAlg(resp)
      .retrieveTopology

    output.map(_ should be(resp))
  }

  it should "redact any password fields" inIO {
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

    output.map(_ should be(expected))
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
        Authorization(BasicCredentials(user, pass)) <- req.headers.get(Authorization)
        if user == correctUsername && pass == correctPassword
      } yield correctUsername
    }.mapF(OptionT.fromOption[IO](_))

  private val middleware: AuthMiddleware[IO, String] =
    AuthMiddleware(authUser)

  def client(service: AuthedRoutes[String, IO]): Client[IO] =
    Client.fromHttpApp[IO](VirtualHost(exact(middleware(service).orNotFound, correctHostname)))

}

class NoOpLogger[F[_] : Applicative] extends Logger[F] {
  override def error(message: => String): F[Unit] = Applicative[F].unit
  override def warn(message: => String): F[Unit] = Applicative[F].unit
  override def info(message: => String): F[Unit] = Applicative[F].unit
  override def debug(message: => String): F[Unit] = Applicative[F].unit
  override def trace(message: => String): F[Unit] = Applicative[F].unit
  override def error(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def warn(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def info(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def debug(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
  override def trace(t: Throwable)(message: => String): F[Unit] = Applicative[F].unit
}

object NoOpLogger {
  def apply[F[_] : Applicative] = new NoOpLogger[F]
}
