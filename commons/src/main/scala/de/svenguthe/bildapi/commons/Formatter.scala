package de.svenguthe.bildapi.commons

import java.text.SimpleDateFormat
import java.util.Locale

import org.joda.time.DateTime


object Formatter {

  var dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  def formatStringToDateTime(dateString: String) : DateTime = {
    new DateTime(dateFormat.parse(dateString))
  }

}
