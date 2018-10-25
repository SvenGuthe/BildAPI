import Dependencies.versions

object Docker {

  lazy val buildOptions = new {
    val dockerVersion = versions.build
    val dockerTagNamespace = "svenguthe"

    val dockerImageNameUrlcrawler = "bild-api-urlcrawler"
    val dockerImageNameCleaner = "bild-api-cleaner"
    val dockerImageNameCrawler = "bild-api-crawler"
    val dockerImageNameDecoderAT = "bild-api-decoderat"
    val dockerImageNameDecoderBA = "bild-api-decoderba"
    val dockerImageNameFilter = "bild-api-filter"
    val dockerImageNameAcitivyTracker = "bild-api-activitytracker"
    val dockerImageNameAcitivyAnalyzer = "bild-api-activityanalyzer"
  }

}
