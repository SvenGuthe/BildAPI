import Dependencies.versions

name := "BildAPI"

// Multi-Project Settings

lazy val commonSettings = Seq(
  version := versions.build,
  organization := "de.svenguthe",
  scalaVersion := versions.scala,
  test in assembly := {}
)

// Multi-Project

lazy val global = project
  .in(file("."))

lazy val commons = project
  .settings(
    commonSettings: _*
  )
  .dependsOn(
    global
  )

lazy val redis_interface = project
  .settings(
    commonSettings: _*
  )
  .dependsOn(
    global
  )

lazy val trigger = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val urlcrawler = project
  .dependsOn(
    commons,
    redis_interface
  )
  .settings(
    commonSettings: _*
  )

lazy val cleaner = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val analyzer = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val api = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val crawler = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val decoder = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val filter = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val storer = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

lazy val visualizer = project
  .dependsOn(
    commons
  )
  .settings(
    commonSettings: _*
  )

// Dependencies

libraryDependencies ++= Seq(
  Dependencies.typesafe,
  Dependencies.scalaTest,
  Dependencies.scalactic,
  Dependencies.slf4jApi,
  Dependencies.slf4jSimple,
)


// Resolvers

resolvers ++= Seq(
  DefaultMavenRepository,
  JavaNet2Repository,
  Resolver.sonatypeRepo("public"),
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeIvyRepo("releases"),
  Resolver.sbtPluginRepo("releases"),
)
