import Docker.buildOptions

libraryDependencies ++= Seq(
  Dependencies.akkaActor,
  Dependencies.akkaCluster,
  Dependencies.akkaRemote,
  Dependencies.scalaScraper,
  Dependencies.sprayJson
)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jdk")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

imageNames in docker := Seq(
  ImageName(
    namespace = Option(buildOptions.dockerTagNamespace),
    repository = buildOptions.dockerImageNameUrlcrawler,
    tag = Option(buildOptions.dockerVersion)
  )
)
