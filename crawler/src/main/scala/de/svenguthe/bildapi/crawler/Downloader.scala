package de.svenguthe.bildapi.crawler

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.{ActivityActorMessages, Actors, HealthcheckMessage, MessageStatus}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.joda.time.DateTime
import org.jsoup.HttpStatusException
import org.slf4j.LoggerFactory

object Downloader {

  private lazy val conf = ConfigFactory.load()
  private lazy val filterActorConfig = conf.getString("filter.actor.address")
  private lazy val className = this.getClass.getCanonicalName
  private lazy val activityTrackerConfig = conf.getString("activitytracker.actor.address")

}

class Downloader extends Actor {

  /**
    * Factories to load the logger
    */

  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  private lazy val filterActor = context.actorSelection(Downloader.filterActorConfig)
  private lazy val activityTrackerActor = context.actorSelection(Downloader.activityTrackerConfig)

  override def receive: Receive = {

    case (message: String, url: String, pubDate : DateTime) =>
      message match {
        case "downloadHTMLfromURL" =>
          val browser = JsoupBrowser()
          try {
            val doc = browser.get(url).toHtml.toString
            logger.info(s"Send document to actor at ${Downloader.filterActorConfig}")
            filterActor ! ("Document", url, doc, pubDate, DateTime.now())
            val healthcheckMessage = HealthcheckMessage(
              Actors.CRAWLER,
              Downloader.className,
              MessageStatus.OK,
              ActivityActorMessages.FORWARDED,
              value = url,
              timestamp = DateTime.now()
            )
            activityTrackerActor ! healthcheckMessage
          } catch {
            case httpStatusException : HttpStatusException =>
              logger.error(s"HTTP-Error while establish HTTP Connection to ${httpStatusException.getUrl} " +
                s"with Status-Code: ${httpStatusException.getStatusCode}")
              val healthcheckMessage = HealthcheckMessage(
                Actors.CRAWLER,
                Downloader.className,
                MessageStatus.FAILURE,
                ActivityActorMessages.HTTPREQUEST,
                value = httpStatusException.toString,
                timestamp = DateTime.now()
              )
              activityTrackerActor ! healthcheckMessage
            case e : Exception =>
              logger.error(s"Error while establish HTTP Connection to $url: $e")
              val healthcheckMessage = HealthcheckMessage(
                Actors.CRAWLER,
                Downloader.className,
                MessageStatus.FAILURE,
                ActivityActorMessages.HTTPREQUEST,
                value = e.toString,
                timestamp = DateTime.now()
              )
              activityTrackerActor ! healthcheckMessage
          }
        case wrongIdentifier =>
          logger.error(s"Downloader-Actor received a wrong identifier String-message: $wrongIdentifier")
          val healthcheckMessage = HealthcheckMessage(
            Actors.CRAWLER,
            Downloader.className,
            MessageStatus.FAILURE,
            ActivityActorMessages.WRONGIDENTIFIER,
            value = wrongIdentifier,
            timestamp = DateTime.now()
          )
          activityTrackerActor ! healthcheckMessage
      }
    case wrongFormat =>
      logger.error(s"Downloader-Actor received non String-message: $wrongFormat")
      val healthcheckMessage = HealthcheckMessage(
        Actors.CRAWLER,
        Downloader.className,
        MessageStatus.FAILURE,
        ActivityActorMessages.WRONGFORMAT,
        value = wrongFormat,
        timestamp = DateTime.now()
      )
      activityTrackerActor ! healthcheckMessage
  }

}
