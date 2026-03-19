import sbt.Keys.scalaVersion

// Supported versions
val scala212 = "2.12.18"
val scala213 = "2.13.11"
val scala33 = "3.3.7"

ThisBuild / description := "Generic WebServices library currently only with Play WS impl./backend"

ThisBuild / organization := "io.cequence"
ThisBuild / scalaVersion := scala213
ThisBuild / version := "0.8.0"
ThisBuild / isSnapshot := false
ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
ThisBuild / crossScalaVersions := List(scala212, scala213, scala33)

// POM settings for Sonatype
ThisBuild / homepage := Some(
  url("https://github.com/cequence-io/ws-client")
)

ThisBuild / sonatypeProfileName := "io.cequence"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/cequence-io/ws-client"),
    "scm:git@github.com:cequence-io/ws-client.git"
  )
)

ThisBuild / developers := List(
  Developer(
    "bburdiliak",
    "Boris Burdiliak",
    "boris.burdiliak@cequence.io",
    url("https://cequence.io")
  ),
  Developer(
    "bnd",
    "Peter Banda",
    "peter.banda@protonmail.com",
    url("https://peterbanda.net")
  )
)

ThisBuild / licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
ThisBuild / publishMavenStyle := true
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / publishTo := sonatypePublishToBundle.value

inThisBuild(
  List(
    scalacOptions += "-Ywarn-unused",
    //    scalaVersion := "2.12.15",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

// Pekko (migrated from Akka for Play 3.0 compatibility)
val pekkoVersion = "1.1.5"
val pekkoHttpVersion = "1.1.0"

lazy val pekkoStreamLibs = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      Seq(
        "com.typesafe.akka" %% "akka-stream" % "2.6.1" exclude("com.typesafe.play", "play-json")
      )
    case Some((2, 13)) =>
      Seq(
        "org.apache.pekko" %% "pekko-stream" % pekkoVersion
      )
    case Some((3, _)) =>
      Seq(
        "org.apache.pekko" %% "pekko-stream" % pekkoVersion
      )
    case _ =>
      throw new Exception("Unsupported scala version")
  }
}

val loggingLibs = Def.setting {
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.4.14" // requires JDK11, in order to use JDK8 switch to 1.3.5
  )
}

// Play WS

def typesafePlayWS(version: String) = Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % version exclude("com.typesafe.play", "play-json"),
  "com.typesafe.play" %% "play-ws-standalone-json" % version exclude("com.typesafe.play", "play-json")
)

def orgPlayWS(version: String) = Seq(
  "org.playframework" %% "play-ahc-ws-standalone" % version,
  "org.playframework" %% "play-ws-standalone-json" % version
)

lazy val playWsDependencies = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      typesafePlayWS("2.1.11")

    case Some((2, 13)) =>
      orgPlayWS("3.0.10")

    case Some((3, _)) =>
      orgPlayWS("3.0.10")

    case _ =>
      orgPlayWS("3.0.10")
  }
}

lazy val playJsonDependency = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => "com.typesafe.play" %% "play-json" % "2.8.2"
    case _             => "org.playframework" %% "play-json" % "3.0.6"
  }
}

lazy val `ws-client-core` =
  (project in file("ws-client-core")).settings(
    name := "ws-client-core",
    libraryDependencies += playJsonDependency.value,
    libraryDependencies += "com.typesafe" % "config" % "1.4.3",
    libraryDependencies ++= loggingLibs.value,
    publish / skip := false
  )

lazy val `ws-client-core-akka` =
  (project in file("ws-client-core-akka"))
    .settings(
      name := "ws-client-core-akka",
      libraryDependencies ++= pekkoStreamLibs.value,
      publish / skip := false
    )
    .dependsOn(`ws-client-core`)

lazy val `json-repair` =
  (project in file("json-repair")).settings(
    name := "json-repair",
    libraryDependencies += playJsonDependency.value,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    libraryDependencies ++= loggingLibs.value,
    publish / skip := false
  )

lazy val `ws-client-play` =
  (project in file("ws-client-play"))
    .settings(
      name := "ws-client-play",
      libraryDependencies ++= playWsDependencies.value,
      publish / skip := false
    )
    .dependsOn(`ws-client-core-akka`)
    .aggregate(`ws-client-core`, `ws-client-core-akka`, `json-repair`)

lazy val `ws-client-play-stream` =
  (project in file("ws-client-play-stream"))
    .settings(
      name := "ws-client-play-stream",
      libraryDependencies += "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion, // JSON WS Streaming
      publish / skip := false
    )
    .dependsOn(`ws-client-core-akka`, `ws-client-play`)
    .aggregate(`ws-client-core`, `ws-client-core-akka`, `ws-client-play`)
