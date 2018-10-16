import Dependencies.versions

name := "BildAPI"

// Multi-Project Settings

lazy val settingDockerTagNamespace = "svenguthe"

lazy val settingCommonSettings = Seq(
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
    settingCommonSettings
  )
  .dependsOn(
    global
  )

lazy val redis_interface = project
  .settings(
    settingCommonSettings
  )
  .dependsOn(
    global
  )

lazy val cassandra_interface = project
  .settings(
    settingCommonSettings
  )
  .dependsOn(
    global,
    commons
  )

lazy val urlcrawler = project
  .dependsOn(
    commons,
    redis_interface
  )
  .settings(
    settingCommonSettings,
  )
  .enablePlugins(
    DockerPlugin
  )

lazy val cleaner = project
  .dependsOn(
    commons,
    redis_interface
  )
  .settings(
    settingCommonSettings
  )

lazy val analyzer = project
  .dependsOn(
    commons
  )
  .settings(
    settingCommonSettings
  )

lazy val api = project
  .dependsOn(
    commons
  )
  .settings(
    settingCommonSettings
  )

lazy val crawler = project
  .dependsOn(
    commons,
    redis_interface
  )
  .settings(
    settingCommonSettings
  )

lazy val decoder = project
  .dependsOn(
    commons,
    cassandra_interface
  )
  .settings(
    settingCommonSettings
  )

lazy val filter = project
  .dependsOn(
    commons
  )
  .settings(
    settingCommonSettings
  )

lazy val visualizer = project
  .dependsOn(
    commons
  )
  .settings(
    settingCommonSettings
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
