lazy val buildSettings = Seq(
  organization := "com.dwolla",
  homepage := Some(url("https://github.com/Dwolla/rabbitmq-topology-backup")),
  description := "Connect to the RabbitMQ API and download the current exchange/queue topology",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  startYear := Option(2019),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.0.0-RC1",
    "com.dwolla" %% "scala-cloudformation-custom-resource" % "4.0.0-M1-SNAPSHOT",
    "org.http4s" %% "http4s-blaze-client" % "0.20.8",
    "org.http4s" %% "http4s-circe" % "0.20.8",
    "org.http4s" %% "http4s-dsl" % "0.20.8",
    "io.circe" %% "circe-literal" % "0.12.0-RC1",
    "io.circe" %% "circe-generic-extras" % "0.12.0-RC1",
    "io.circe" %% "circe-optics" % "0.11.0",
    "com.dwolla" %% "testutils-scalatest-fs2" % "2.0.0-M1" % Test,
  ),
)

lazy val `rabbitmq-topology-backup` = (project in file("."))
  .settings(buildSettings ++ noPublishSettings: _*)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  Keys.`package` := file(""),
)

val documentationSettings = Seq(
  autoAPIMappings := true,
  apiMappings ++= {
    // Lookup the path to jar (it's probably somewhere under ~/.ivy/cache) from computed classpath
    val classpath = (fullClasspath in Compile).value
    def findJar(name: String): File = {
      val regex = ("/" + name + "[^/]*.jar$").r
      classpath.find { jar => regex.findFirstIn(jar.data.toString).nonEmpty }.get.data // fail hard if not found
    }

    // Define external documentation paths
    Map(
      findJar("circe-generic-extra") -> url("http://circe.github.io/circe/api/io/circe/index.html"),
    )
  }
)
