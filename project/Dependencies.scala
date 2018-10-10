import sbt._

object Dependencies {

  // Typesafe
  lazy val typesafe = "com.typesafe" % "config" % versions.typesafe

  // Akka
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % versions.akkaActor
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % versions.akkaStream
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % versions.akkaHttp
  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % versions.akkaCluster
  lazy val akkaRemote = "com.typesafe.akka" %% "akka-remote" % versions.akkaRemote

  // Scala Test
  lazy val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest
  lazy val scalactic = "org.scalactic" %% "scalactic" % versions.scalactic

  // Logging
  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % versions.slf4j
  lazy val slf4jSimple = "org.slf4j" % "slf4j-simple" % versions.slf4j

  // Redis
  lazy val redis = "net.debasishg" %% "redisclient" % versions.redis

  // HTML Parser
  lazy val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % versions.scalaScraper

  // Datetime
  lazy val joda = "joda-time" % "joda-time" % versions.joda

  // Spark

  lazy val versions = new {
    val scala = "2.12.6"
    val build = "0.1"
    val typesafe = "1.3.2"
    val akkaActor = "2.5.16"
    val akkaStream = "2.5.16"
    val akkaHttp = "10.1.5"
    val akkaCluster = "2.5.16"
    val akkaRemote = "2.5.16"
    val scalaTest = "3.0.5"
    val scalactic = "3.0.5"
    val typesafeLogging = "3.9.0"
    val logback = "1.2.3"
    val slf4j = "1.7.5"
    val redis = "3.8"
    val scalaScraper = "2.1.0"
    val joda = "2.10"
  }

}
