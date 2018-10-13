package de.svenguthe.bildapi.crawler

import akka.actor.Actor
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.joda.time.DateTime
import org.jsoup.HttpStatusException
import org.slf4j.LoggerFactory

object Downloader {

  private lazy val destinationActor = "akka.tcp://FilterActorSystem@127.0.0.1:2553/user/Filter"

}

class Downloader extends Actor {

  /**
    * Factories to load the logger
    */

  private lazy val logger = LoggerFactory.getLogger(this.getClass)

 private lazy val actorSelection = context.actorSelection(Downloader.destinationActor)

  override def receive: Receive = {

    case (message: String, url: String, pubDate : DateTime) =>
      message match {
        case "downloadHTMLfromURL" =>
          val browser = JsoupBrowser()
          try {
            val doc = browser.get(url).toHtml.toString
            logger.info(s"Send document to actor at ${Downloader.destinationActor}")
            actorSelection ! ("Document", url, doc, pubDate)
          } catch {
            case httpStatusException : HttpStatusException =>
              logger.error(s"HTTP-Error while establish HTTP Connection to ${httpStatusException.getUrl} " +
                s"with Status-Code: ${httpStatusException.getStatusCode}")
            case e : Exception =>
              logger.error(s"Error while establish HTTP Connection to $url: $e")
          }
        case unknownMessage =>
          logger.error(s"Downloader-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Downloader-Actor received non String-message")
  }

}
