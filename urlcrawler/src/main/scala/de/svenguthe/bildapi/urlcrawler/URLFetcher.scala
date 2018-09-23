package de.svenguthe.bildapi.urlcrawler

import java.util.Calendar

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

class URLFetcher extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val crawler = context.actorOf(Props[Crawler], "Crawler")
  private val calendar = Calendar.getInstance()

  private val redisConnection = RedisService.getRedisConnection

  private val conf = ConfigFactory.load()

  def receive = {
    case msg: String =>
      msg match {
        case "initalizeFetching" =>
          redisURLToCrawler
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
      }
    case wrongFormat =>
      logger.error(s"URLFetcher received wrong message type: $wrongFormat")
  }

  def redisURLToCrawler: Unit ={
    val keyValue = redisConnection.keys("*")
    logger.info(s"All keys: ${keyValue.mkString(", ")}")

    keyValue match {
      case Some(map) =>
        map.foreach(
          url => {
            crawler ! (url.getOrElse(""), calendar.getTime)
          }
        )
      case None =>
    }
  }

}