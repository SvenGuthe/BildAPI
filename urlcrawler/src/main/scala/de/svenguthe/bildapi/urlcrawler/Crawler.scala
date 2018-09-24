package de.svenguthe.bildapi.urlcrawler

import java.util.Date

import akka.actor.Actor
import org.slf4j.LoggerFactory

class Crawler extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def receive: Receive = {
    case (url : String, timestamp : Date) =>
      logger.info(s"Crawler received Message - URL: $url, Timestamp: $timestamp")
      CrawlerService.getLinks(url, context.sender)
    case wrongFormat =>
      logger.error(s"Crawler received Message in wrong format: $wrongFormat")
  }

}
