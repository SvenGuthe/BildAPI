package de.svenguthe.bildapi.commons.datatypes

import org.joda.time.DateTime

case class BildArticle(
                      var url : String,
                      var pubDate : DateTime,
                      var subChannel1 : String = "",
                      var subChannel2 : String = "",
                      var subChannel3 : String = "",
                      var subChannel4 : String = "",
                      var kicker : String = "",
                      var headline : String = "",
                      var subhead : String = "",
                      var text : String = "",
                      var crosshaedings : List[String] = List(),
                      var documentid : String = "",
                      var keywords : List[String] = List(),
                      var crawlerTime : DateTime
                      )