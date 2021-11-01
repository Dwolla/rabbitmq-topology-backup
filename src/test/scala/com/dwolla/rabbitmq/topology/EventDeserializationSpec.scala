package com.dwolla.rabbitmq.topology

import com.dwolla.rabbitmq.topology.model.{RabbitMQConfig, Username}
import com.eed3si9n.expecty.Expecty.expect
import io.circe.literal._
import munit.CatsEffectSuite
import org.http4s.syntax.all._

class EventDeserializationSpec extends CatsEffectSuite {

  test("event deserialization should deserialize an example event") {
    val input =
      json"""{
               "baseUri": "https://rabbit.us-west-2.local.dwolla.net",
               "username": "guest",
               "password": "password"
             }"""

    val expected = RabbitMQConfig(uri"https://rabbit.us-west-2.local.dwolla.net", "guest".asInstanceOf[Username], "password")
    expect(input.as[RabbitMQConfig].contains(expected))
  }

}
