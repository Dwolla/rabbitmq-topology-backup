package com.dwolla.lambda

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}

import java.io._
import com.dwolla.rabbitmq.topology.LambdaHandler
import io.circe.literal._
import org.slf4j.LoggerFactory

object TestRunner extends App {
  val logger = LoggerFactory.getLogger("TestRunner")

  val input: InputStream = new ByteArrayInputStream(
//             "baseUri": "http://localhost:15672",
    json"""{
             "baseUri": "https://google.com",
             "username": "guest",
             "password": "AQICAHh38+DAqADvcRLU4+t2AYhr82YbZuuFQdjdX95NTppHhwHd8XtgUIF6t8gP+mKlCrizAAAAYzBhBgkqhkiG9w0BBwagVDBSAgEAME0GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMuvCOEA4D/QGIaihbAgEQgCATYPnSoUh0UI+QsqlR00kP7cGLdyh6fUrfBv7Gzt8ToA=="
           }""".noSpaces.getBytes)
  val badInput: InputStream = new ByteArrayInputStream("{Bad input".getBytes)
  val output: ByteArrayOutputStream = new ByteArrayOutputStream()

  new LambdaHandler().handleRequest(input, output, new EmptyContext("test-runner"))

  logger.info("{}", output.size())
}

class EmptyContext(functionName: String) extends Context {
  override def getAwsRequestId: String = ""
  override def getLogGroupName: String = null
  override def getLogStreamName: String = null
  override def getFunctionName: String = functionName
  override def getFunctionVersion: String = ""
  override def getInvokedFunctionArn: String = ""
  override def getIdentity: CognitoIdentity = null
  override def getClientContext: ClientContext = null
  override def getRemainingTimeInMillis: Int = -1
  override def getMemoryLimitInMB: Int = -1
  override def getLogger: LambdaLogger = new LambdaLogger {
    override def log(message: String): Unit = println(message)
    override def log(message: Array[Byte]): Unit = println(new String(message))
  }
}
