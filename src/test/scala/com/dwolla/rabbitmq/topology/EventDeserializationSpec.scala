package com.dwolla.rabbitmq.topology

import com.dwolla.rabbitmq.topology.model.{RabbitMQConfig, Username}
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import io.circe.literal._
import org.http4s.syntax.all._

class EventDeserializationSpec extends FlatSpec with Matchers with EitherValues {

  behavior of "event deserialization"

  it should "deserialize an example event" in {
    val input =
      json"""{
               "baseUri": "https://rabbit.us-west-2.local.dwolla.net",
               "username": "guest",
               "password": "password"
             }"""

    val expected = RabbitMQConfig(uri"https://rabbit.us-west-2.local.dwolla.net", "guest".asInstanceOf[Username], "password")
    input.as[RabbitMQConfig].contains(expected) should be(true)
  }

}
