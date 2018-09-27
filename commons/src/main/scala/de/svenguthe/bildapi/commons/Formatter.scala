package de.svenguthe.bildapi.commons

import java.text.SimpleDateFormat
import java.util.{Date, Locale}


object Formatter {

  var dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

  def formatStringToDate(dateString: String) : Date = {
    dateFormat.parse(dateString)
  }

}
