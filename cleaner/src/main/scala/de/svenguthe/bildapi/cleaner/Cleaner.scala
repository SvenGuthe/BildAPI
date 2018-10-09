package de.svenguthe.bildapi.cleaner

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

/** The [[Cleaner]] is an Actor which deletes all the received messages from Redis
  *
  */
class Cleaner extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()


  /** When Receiving new Messages from other actors like [[OutdatedURLFetcher]]
    *
    */
  override def receive: Receive = {
    /**
      * If the Message is a String (URL), then the Actor will delete this entry from redis
      */
    case url: String =>
      logger.info(s"Clean up URL: $url")
      redisConnection.del(url)

    /**
      * If the Message is in an other format, this cannot be parsed
      */
    case wrongFormat =>
      logger.error(s"Cleaner received wrong message type: $wrongFormat")
  }

}
