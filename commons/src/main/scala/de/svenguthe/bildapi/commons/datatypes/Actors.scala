package de.svenguthe.bildapi.commons.datatypes

object Actors extends Enumeration {
  type Actors = Value

  val CRAWLER = Value
  val FILTER = Value
  val DECODERBA = Value
  val DECODERTA = Value
  val CLEANER = Value
  val URLCrawler = Value
}
