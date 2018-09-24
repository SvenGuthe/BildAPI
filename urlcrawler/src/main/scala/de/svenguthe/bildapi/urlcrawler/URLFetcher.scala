package de.svenguthe.bildapi.urlcrawler

import java.util.Calendar

import akka.actor.{Actor, Props}
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

class URLFetcher extends Actor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val crawler = context.actorOf(Props[Crawler], "Crawler")
  private val calendar = Calendar.getInstance()

  private val conf = ConfigFactory.load()

  lazy val redisConnection = RedisService.getRedisConnection

  def receive = {
    case msg: String =>
      msg match {
        case "initalizeFetching" =>
          redisURLToCrawler(redisConnection)
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
      }
    case (msg : String, url : String) =>
      msg match {
        case "publishURL" =>
          redisConnection.get(url) match {
            case Some(_) =>
            case None =>
              logger.info(s"Set key $url")
              redisConnection.set(url, Calendar.getInstance().getTime)
          }
        case "failure" =>
          logger.info(s"Delete key $url")
          redisConnection.del(url)
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier and URL: $url")
      }
    case wrongFormat =>
      logger.error(s"URLFetcher received wrong message type: $wrongFormat")
  }

  def redisURLToCrawler(redisConnection : RedisClient): Unit ={
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