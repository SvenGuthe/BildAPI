package de.svenguthe.bildapi.crawler

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import de.svenguthe.bildapi.commons.datatypes.{ActivityActorMessages, Actors, HealthcheckMessage, MessageStatus}
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

object Crawler {

  /**
    * Factories to load the typesafe-configuration
    */
  private lazy val conf = ConfigFactory.load()

  private lazy val downloaderConfig = conf.getString("crawlerSystem.akka.actor.actors.downloader")
  private lazy val className = this.getClass.getCanonicalName
  private lazy val activityTrackerConfig = conf.getString("activitytracker.actor.address")

}

class Crawler extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  /**
    * Define the [[Downloader]]-Actor
    */
  private lazy val downloader = context.actorOf(Props[Downloader], Crawler.downloaderConfig)
  private lazy val actorSelection = context.actorSelection(Crawler.activityTrackerConfig)


  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()

  override def receive: Receive = {
    case message : String =>
      message match {
        case "startCrawling" =>
          logger.info("Crawler-Actor received a message to start crawling")
          val healthcheckMessage = HealthcheckMessage(
            Actors.CRAWLER,
            Crawler.className,
            MessageStatus.OK,
            ActivityActorMessages.LOG,
            value = message,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
          val allKeysFromRedis = RedisService.getAllKeysFromRedis(redisConnection)

          allKeysFromRedis.get.foreach(url => {

            val pubDate = redisConnection.get[String](url.getOrElse("")).getOrElse("")
            val pubDateJoda = Formatter.formatStringToDateTime(pubDate)

            if(pubDateJoda.year.get() == 9999){
              logger.info(s"Try to crawl no article at ${url.getOrElse("")}")
            } else{
              val urlString = url.getOrElse("")
              logger.info(s"Crawl Article at $urlString")
              if(urlString.nonEmpty) downloader ! ("downloadHTMLfromURL", urlString, pubDateJoda)
            }

          })

        case wrongIdentifier =>
          logger.error(s"Crawler-Actor received a wrong identifier String-message: $wrongIdentifier")
          val healthcheckMessage = HealthcheckMessage(
            Actors.CRAWLER,
            Crawler.className,
            MessageStatus.FAILURE,
            ActivityActorMessages.WRONGIDENTIFIER,
            value = wrongIdentifier,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
      }
    case wrongFormat =>
      logger.error(s"Crawler-Actor received non String-message: $wrongFormat")
      val healthcheckMessage = HealthcheckMessage(
        Actors.CRAWLER,
        Crawler.className,
        MessageStatus.FAILURE,
        ActivityActorMessages.WRONGFORMAT,
        value = wrongFormat,
        timestamp = DateTime.now()
      )
      actorSelection ! healthcheckMessage
  }

}
