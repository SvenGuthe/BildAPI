package de.svenguthe.bildapi.commons.datatypes

object Actors extends Enumeration {
  type Actors = Value

  val CRAWLER = Value("crawler")
  val FILTER = Value("filter")
  val DECODER = Value("decoder")
  val CLEANER = Value("cleaner")
  val URLCrawler = Value("urlcrawler")
}
