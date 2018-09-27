package de.svenguthe.bildapi.cleaner

import java.util.Date

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import org.slf4j.LoggerFactory

class Cleaner extends Actor {

  lazy val logger = LoggerFactory.getLogger(this.getClass)
  lazy val conf = ConfigFactory.load()

  override def receive: Receive = {
    case (url: String, date: String) =>
      logger.info(s"URL: $url, Date: ${Formatter.formatStringToDate(date)}")
    case wrongFormat =>
      logger.error(s"Cleaner received wrong message type: $wrongFormat")
  }

}
