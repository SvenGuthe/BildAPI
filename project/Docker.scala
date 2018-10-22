import Dependencies.versions

object Docker {

  lazy val buildOptions = new {
    val dockerVersion = versions.build
    val dockerTagNamespace = "svenguthe"

    val dockerImageNameUrlcrawler = "bild-api-urlcrawler"
    val dockerImageNameCleaner = "bild-api-cleaner"
    val dockerImageNameCrawler = "bild-api-crawler"
    val dockerImageNameDecoderAT = "bild-api-decoderAT"
    val dockerImageNameDecoderBA = "bild-api-decoderBA"
    val dockerImageNameFilter = "bild-api-filter"
    val dockerImageNameAcitivyTracker = "bild-api-activitytracker"
  }

}
