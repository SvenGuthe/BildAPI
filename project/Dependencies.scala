import sbt._

object Dependencies {

  // Typesafe
  lazy val typesafe = "com.typesafe" % "config" % versions.typesafe

  // Akka
  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % versions.akkaActor
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % versions.akkaStream
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % versions.akkaHttp
  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % versions.akkaCluster

  // Scala Test
  lazy val scalaTest = "org.scalatest" %% "scalatest" % versions.scalaTest
  lazy val scalactic = "org.scalactic" %% "scalactic" % versions.scalactic


  // Spark

  lazy val versions = new {
    lazy val scala = "2.12.6"
    lazy val build = "0.1"
    lazy val typesafe = "1.3.2"
    lazy val akkaActor = "2.5.16"
    lazy val akkaStream = "2.5.16"
    lazy val akkaHttp = "10.1.5"
    lazy val akkaCluster = "2.5.16"
    lazy val scalaTest = "3.0.5"
    lazy val scalactic = "3.0.5"
  }

}
