package com.dwolla.lambda

import java.io._

import com.dwolla.rabbitmq.topology.Lambda
import io.circe.Printer
import io.circe.literal._

object TestRunner extends App {

  val input: InputStream = new ByteArrayInputStream(json"null".noSpaces.getBytes)
  val output: ByteArrayOutputStream = new ByteArrayOutputStream()

  new Lambda(Printer.spaces2).handleRequest(input, output, null)

  println(output)
}
