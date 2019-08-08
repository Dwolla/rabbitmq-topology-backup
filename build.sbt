import sbtassembly.Log4j2MergeStrategy

lazy val buildSettings = Seq(
  scalaVersion := "2.12.8",
  organization := "com.dwolla",
  homepage := Some(url("https://github.com/Dwolla/rabbitmq-topology-backup")),
  description := "Connect to the RabbitMQ API and download the current exchange/queue topology",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  startYear := Option(2019),
  libraryDependencies ++= Seq(
    "software.amazon.awssdk" % "kms" % "2.7.18",
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
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if ps.last == "io.netty.versions.properties" =>
      MergeStrategy.concat
    case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" =>
      Log4j2MergeStrategy.plugincache
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  },
  assemblyJarName in assembly := normalizedName.value + ".jar"
)

lazy val `rabbitmq-topology-backup` = (project in file("."))
  .settings(buildSettings: _*)

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
