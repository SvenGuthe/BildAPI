import Docker.buildOptions

libraryDependencies ++= Seq(
  Dependencies.akkaActor,
  Dependencies.akkaCluster,
  Dependencies.akkaRemote
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

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
    repository = buildOptions.dockerImageNameDecoderBA,
    tag = Option(buildOptions.dockerVersion)
  )
)
