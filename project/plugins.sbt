addSbtPlugin("com.dwijnand" % "sbt-travisci" % "1.2.0")

resolvers += Resolver.bintrayIvyRepo("dwolla", "sbt-plugins")
addSbtPlugin("com.dwolla.sbt" % "sbt-dwolla-base" % "1.4.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
addSbtPlugin("com.github.nomadblacky" % "sbt-assembly-log4j2" % "0.1.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")
