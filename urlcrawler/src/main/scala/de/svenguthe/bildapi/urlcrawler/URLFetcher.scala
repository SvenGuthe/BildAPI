package de.svenguthe.bildapi.urlcrawler

import java.util.Date

import akka.actor.{Actor, Props}
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap

/**
  * Fetches all the URLs from Redis and send them to the [[Crawler]]-Actor
  */
class
URLFetcher extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val crawlerConfig = conf.getString("akka.actors.crawler")


  /**
    * Define the [[Crawler]]-Actor
    */
  private lazy val crawler = context.actorOf(Props[Crawler], crawlerConfig)

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisInstance = RedisService.getRedisServiceInstance()
  private lazy val redisConnection = redisInstance.getRedisConnection()

  private lazy val urlHashMap = HashMap[String, Date]()

  override def receive: Receive = {
    case msg: String =>
      msg match {
        case "initalizeFetching" =>
          redisURLToCrawler(redisConnection)
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
      }
    case (msg : String, url : String, date: Date) =>
      msg match {
        case "publishURL" =>
          urlHashMap.get(url) match {
            case Some(_) =>
              logger.info(s"Key allready existing")
            case None =>
              logger.info(s"Set key $url")
              redisConnection.set(url, date)
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
    logger.debug(s"All keys: ${keyValue.mkString(", ")}")

    keyValue match {
      case Some(map) =>
        map.foreach(
          url => {
            urlHashMap.put(url.getOrElse(""), DateTime.now().toDate)
            crawler ! (url.getOrElse(""), DateTime.now().toDate)
          }
        )
      case None =>
    }
  }

}