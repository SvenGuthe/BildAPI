package de.svenguthe.bildapi.commons

import java.text.SimpleDateFormat
import java.util.Locale

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


object Formatter {

  val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  var dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)

  def formatStringToDateTime(dateString: String) : DateTime = {
    new DateTime(dateFormat.parse(dateString))
  }

  def formatStringToDateTimeCrawler(pubdate : String): DateTime ={
    dtf.parseDateTime(pubdate)
  }

}
