import sbtassembly.Log4j2MergeStrategy

lazy val buildSettings = Seq(
  organization := "com.dwolla",
  homepage := Some(url("https://github.com/Dwolla/rabbitmq-topology-backup")),
  description := "Connect to the RabbitMQ API and download the current exchange/queue topology",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  startYear := Option(2019),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  resolvers ++= Seq(
    Resolver.bintrayRepo("dwolla", "maven"),
    Resolver.sonatypeRepo("releases"),
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
  .settings(
    libraryDependencies ++= {
      val http4sVersion = "0.21.0-M5"
      val fs2AwsVersion = "2.0.0-M5"
      Seq(
        "software.amazon.awssdk" % "kms" % "2.7.18",
        "com.dwolla" %% "fs2-aws-java-sdk2" % fs2AwsVersion,
        "com.dwolla" %% "fs2-aws-lambda-io-app" % fs2AwsVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "com.dwolla" %% "testutils-scalatest-fs2" % "2.0.0-M3" % Test,
        "org.http4s" %% "http4s-server" % http4sVersion % Test,
      )
    },
  )
