package com.dwolla.rabbitmq.topology

import com.dwolla.rabbitmq.topology.model.{RabbitMQConfig, Username}
import io.circe.literal._
import org.http4s.syntax.all._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventDeserializationSpec extends AnyFlatSpec with Matchers with EitherValues {

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
