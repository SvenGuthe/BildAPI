package de.svenguthe.bildapi.crawler

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

object Crawler {

  /**
    * Factories to load the typesafe-configuration
    */
  private lazy val conf = ConfigFactory.load()

  private lazy val downloaderConfig = conf.getString("crawlerSystem.akka.actor.actors.downloader")

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

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()

  override def receive: Receive = {
    case message : String =>
      message match {
        case "startCrawling" =>
          logger.info("Crawler-Actor received a message to start crawling")
          val allKeysFromRedis = RedisService.getAllKeysFromRedis(redisConnection)

          try {
            allKeysFromRedis.get.foreach(url => {

              val pubDate = redisConnection.get[String](url.getOrElse("")).getOrElse("")
              val pubDateJoda = Formatter.formatStringToDateTime(pubDate)

              if(pubDateJoda.year.get() == 9999){
                logger.info(s"Try to crawl no article at ${url.getOrElse("")}")
              } else{
                val urlString = url.get
                logger.info(s"Crawl Article at $urlString")
                downloader ! ("downloadHTMLfromURL", urlString, pubDateJoda)
              }

            })
          } catch {
            case e : Exception => logger.error(s"Error while reading keys from redis: $e")
          }

        case unknownMessage =>
          logger.error(s"Crawler-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Crawler-Actor received non String-message")
  }

}
