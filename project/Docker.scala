import Dependencies.versions

object Docker {

  lazy val buildOptions = new {
    val dockerVersion = versions.build
    val dockerTagNamespace = "svenguthe"
    val dockerImageNameUrlcrawler = "bild-api-urlcrawler"
  }

}
