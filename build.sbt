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
  val scalatest = "3.0.0"
  val oraclejdbc = "12.1.0.1-atlassian-hosted"
  val postgresqljdbc = "9.4.1211"
  val bcrypt = "2.6"
}

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "org.scalatest" %% "scalatest" % versions.scalatest % "test",
  "com.oracle" % "ojdbc6" % versions.oraclejdbc,
  "org.postgresql" % "postgresql" % versions.postgresqljdbc,
  "com.github.t3hnar" %% "scala-bcrypt" % versions.bcrypt
)