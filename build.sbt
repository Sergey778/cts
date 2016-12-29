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
  val finatra = "2.7.0"
  val logback = "1.1.7"
  val scalatest = "3.0.0"
  val oraclejdbc = "12.1.0.1-atlassian-hosted"
  val postgresqljdbc = "9.4.1211"
  val bcrypt = "2.6"
  val scalaXml = "1.0.6"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-httpclient" % versions.finatra,

  "com.lihaoyi" %% "scalatags" % "0.6.1",

  "commons-dbcp" % "commons-dbcp" % "1.4",
  "commons-pool" % "commons-pool" % "1.6",
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "2.4.2"   % "test",
  "javax.mail" % "mail" % "1.5.0-b01",

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
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "org.scala-lang.modules" % "scala-xml_2.11" % versions.scalaXml
)

mainClass in assembly := Some("runner.ServerRunner")
test in assembly := {}
assemblyMergeStrategy in assembly :=  {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    xs map {
      _.toLowerCase
    } match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.rename
    }
  case _ => MergeStrategy.first
}