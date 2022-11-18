ThisBuild / organization := "com.dwolla"
ThisBuild / homepage := Some(url("https://github.com/Dwolla/rabbitmq-topology-backup"))
ThisBuild / description := "Connect to the RabbitMQ API and download the current exchange/queue topology"
ThisBuild / licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := Option(2019)
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"), JavaSpec.temurin("8"))
ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "Universal / packageBin"), name = Option("Build, Test, and Package")))
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / developers ++= List(
  Developer("Dwolla", "Dwolla Dev Team", s"dev+${name.value}@dwolla.com", url("https://dwolla.com")),
  Developer("bpholt", "Brian Holt", "@bpholt", url("https://dwolla.com")),
)

lazy val `rabbitmq-topology-backup` = (project in file("."))
  .settings(
    maintainer := developers.value.headOption.map(dev => s"${dev.name} <${dev.email}>").getOrElse("No developers are set on the project"),
    libraryDependencies ++= {
      val http4sVersion = "0.23.12"
      val circeVersion = "0.14.2"
      val natchezVersion = "0.1.6"
      val feralVersion = "0.1.0-M9"
      val fs2Version = "3.2.7"
      val log4CatsVersion = "2.3.1"

      Seq(
        "org.typelevel" %% "feral-lambda" % feralVersion,
        "org.http4s" %% "http4s-ember-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.tpolecat" %% "natchez-core" % natchezVersion,
        "org.tpolecat" %% "natchez-xray" % natchezVersion,
        "co.fs2" %% "fs2-reactive-streams" % fs2Version,
        "co.fs2" %% "fs2-io" % fs2Version,
        "org.typelevel" %% "cats-tagless-macros" % "0.14.0",
        "com.comcast" %% "ip4s-core" % "3.2.0",
        "io.circe" %% "circe-generic" % circeVersion,
        "com.chuusai" %% "shapeless" % "2.3.9",
        "org.tpolecat" %% "natchez-http4s" % "0.3.2",
        "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion,
        "com.dwolla" %% "fs2-aws-java-sdk2" % "3.0.0-RC1",
        "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.2" % Runtime,
        "com.amazonaws" % "aws-lambda-java-log4j2" % "1.5.1" % Runtime,
        "software.amazon.awssdk" % "kms" % "2.17.255" % Runtime,
        "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
        "com.eed3si9n.expecty" %% "expecty" % "0.15.4" % Test,
        "org.http4s" %% "http4s-server" % http4sVersion % Test,
        "io.circe" %% "circe-literal" % circeVersion % Test,
        "io.circe" %% "circe-parser" % circeVersion % Test,
        "org.typelevel" %% "log4cats-noop" % log4CatsVersion,
      )
    },
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
  .enablePlugins(UniversalPlugin, JavaAppPackaging)
