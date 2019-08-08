package com.dwolla.lambda

import java.io._

import com.dwolla.rabbitmq.topology.LambdaHandler
import io.circe.Printer
import io.circe.literal._

object TestRunner extends App {

  val input: InputStream = new ByteArrayInputStream(
    json"""{
             "hostname": "https://rabbit.us-west-2.sandbox.dwolla.net",
             "username": "guest",
             "password": "AQICAHh38+DAqADvcRLU4+t2AYhr82YbZuuFQdjdX95NTppHhwHd8XtgUIF6t8gP+mKlCrizAAAAYzBhBgkqhkiG9w0BBwagVDBSAgEAME0GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMuvCOEA4D/QGIaihbAgEQgCATYPnSoUh0UI+QsqlR00kP7cGLdyh6fUrfBv7Gzt8ToA=="
           }""".noSpaces.getBytes)
  val output: ByteArrayOutputStream = new ByteArrayOutputStream()

  new LambdaHandler(Printer.spaces2).handleRequest(input, output, null)

  println(output)
}
