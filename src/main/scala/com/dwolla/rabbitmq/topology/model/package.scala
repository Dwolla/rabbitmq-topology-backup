package com.dwolla.rabbitmq.topology

import org.http4s._
import io.circe.generic.semiauto._
import org.http4s.circe._
import io.circe._
import shapeless.tag
import shapeless.tag.@@

package object model {
  type RabbitMqTopology = Json

  type Username = String @@ UsernameTag
  type Password = String @@ PasswordTag

  val tagUsername: String => Username = tag[UsernameTag][String]
  val tagPassword: String => Password = tag[PasswordTag][String]

  implicit def taggedStringEncoder[T]: Encoder[String @@ T] = Encoder[String].contramap(identity)
  implicit def taggedStringDecoder[T]: Decoder[String @@ T] = Decoder[String].map(tag[T][String])
}

package model  {
  case class RabbitMQConfig(hostname: Uri, username: Username, password: String)
  object RabbitMQConfig {
    implicit def rabbitMQConfigEncoder[F[_]]: Encoder[RabbitMQConfig] = deriveEncoder
    implicit def rabbitMQConfigDecoder[F[_]]: Decoder[RabbitMQConfig] = deriveDecoder
  }

  trait UsernameTag
  trait PasswordTag
}
