lazy val buildSettings = Seq(
  organization := "com.dwolla",
  maintainer := s"dev+${name.value}@dwolla.com",
  homepage := Some(url("https://github.com/Dwolla/rabbitmq-topology-backup")),
  description := "Connect to the RabbitMQ API and download the current exchange/queue topology",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  startYear := Option(2019),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  Compile / packageDoc / mappings := Seq(),
  Compile / packageDoc / publishArtifact := false,
  topLevelDirectory := None,
  Universal / packageName := name.value,
  Compile / scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
      case _ => Nil
    }
  },

  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => Nil
      case _ => compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) :: Nil
    }
  },
)

lazy val `rabbitmq-topology-backup` = (project in file("."))
  .settings(buildSettings: _*)
  .settings(
    libraryDependencies ++= {
      val http4sVersion = "0.21.1"
      val circeVersion = "0.13.0"
      val fs2AwsVersion = "2.0.0-M9"
      val amazonXRayVersion = "2.4.0"
      Seq(
        "com.amazonaws" % "aws-xray-recorder-sdk-core" % amazonXRayVersion,
        "com.amazonaws" % "aws-xray-recorder-sdk-aws-sdk-v2-instrumentor" % amazonXRayVersion,
        "software.amazon.awssdk" % "kms" % "2.7.18",
        "com.dwolla" %% "fs2-aws-java-sdk2" % fs2AwsVersion,
        "com.dwolla" %% "fs2-aws-lambda-io-app" % fs2AwsVersion,
        "org.http4s" %% "http4s-ember-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "com.dwolla" %% "natchez-aws-xray" % "0.0.1",
        "com.dwolla" %% "testutils-scalatest-fs2" % "2.0.0-M4" % Test,
        "org.http4s" %% "http4s-server" % http4sVersion % Test,
        "io.circe" %% "circe-literal" % circeVersion % Test,
        "io.circe" %% "circe-parser" % circeVersion % Test,
        "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1" % Test,
      )
    },
  )
  .enablePlugins(UniversalPlugin, JavaAppPackaging)
