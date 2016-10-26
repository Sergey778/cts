name := "cts-server"

version := "1.0"

scalaVersion := "2.11.8"

version := "0.1.0-M1"

fork in run := true

javaOptions ++= Seq(
  "-Dlog.service.output=/dev/stderr",
  "-Dlog.access.output=/dev/stderr"
)

lazy val versions = new {
  val finatra = "2.5.0"
  val logback = "1.1.7"
  val scalatest = "2.2.6"
  val oraclejdbc = "12.1.0.1-atlassian-hosted"
  val postgresqljdbc = "9.4.1211"
  val bcrypt = "2.6"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-httpclient" % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "org.scalatest" %% "scalatest" % versions.scalatest % "test",
  "com.oracle" % "ojdbc6" % versions.oraclejdbc,
  "org.postgresql" % "postgresql" % versions.postgresqljdbc,
  "com.github.t3hnar" %% "scala-bcrypt" % versions.bcrypt,

  "com.twitter" %% "finatra-http" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "finatra-jackson" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-server" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-app" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-core" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % versions.finatra % "test" classifier "tests",

  "com.twitter" %% "finatra-http" % versions.finatra % "test",
  "com.twitter" %% "finatra-jackson" % versions.finatra % "test",
  "com.twitter" %% "inject-server" % versions.finatra % "test",
  "com.twitter" %% "inject-app" % versions.finatra % "test",
  "com.twitter" %% "inject-core" % versions.finatra % "test",
  "com.twitter" %% "inject-modules" % versions.finatra % "test",
  "com.google.inject.extensions" % "guice-testlib" % "4.0" % "test",

  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.specs2" %% "specs2" % "2.3.12" % "test"
)