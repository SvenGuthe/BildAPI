import sbt._

// Plugins
addSbtPlugin(scalaStyle)
addSbtPlugin(scalafmt)
addSbtPlugin(sbtAssembly)
addSbtPlugin(sbtDocker)

// SBT Docker
lazy val sbtDocker = "se.marcuslonnberg" % "sbt-docker" % versions.sbtDocker

// Scala Style
lazy val scalaStyle = "org.scalastyle" % "scalastyle-sbt-plugin" % versions.scalaStyle

// Scalafmt
lazy val scalafmt = "com.lucidchart" % "sbt-scalafmt-coursier" % versions.scalafmt

// sbt assembly
lazy val sbtAssembly = "com.eed3si9n" % "sbt-assembly" % versions.sbtAssembly

lazy val versions = new {
  val sbtDocker = "1.5.0"
  val scalaStyle = "1.0.0"
  val scalafmt = "1.15"
  val sbtAssembly = "0.14.7"
}