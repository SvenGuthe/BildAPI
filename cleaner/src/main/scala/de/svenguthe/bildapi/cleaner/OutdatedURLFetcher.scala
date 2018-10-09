package de.svenguthe.bildapi.cleaner

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.{DateTime, Days}
import org.slf4j.LoggerFactory

/** The [[OutdatedURLFetcher]] is an Actor which fetches all entries from redis and test their publishing date
  * if this date was within the last 7 days, nothing will happened
  * if this date wasn't within the last 7 days, the entry will be deleted
  */
class OutdatedURLFetcher extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val cleanerConfig = conf.getString("akka.actors.cleaner")

  /**
    * Define the [[Cleaner]]-Actor
    */
  private val cleaner = context.actorOf(Props[Cleaner], cleanerConfig)

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisInstance = RedisService.getRedisServiceInstance()
  private lazy val redisConnection = redisInstance.getRedisConnection

  /** When Receiving new Messages from other actors like [[Main]]
    *
    */
  override def receive: Receive = {
    /**
      * If the Message is an String and is equal to "startCleaning" - then the actor will fetch all Entries from redis and clean them
      */
    case msg: String =>
      msg match {
        case "startCleaning" =>
          sendEntriesToCleaner("*")

        /**
          * If the Message was an String but not equal to "startCleaning" - then the actor can't parse this action and logs an error
          */
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
      }

    /**
      * If the Message wasn't a String - then the actor can't parse this Message and logs an error
      */
    case wrongFormat =>
      logger.error(s"URLFetcher received wrong message type: $wrongFormat")
  }

  /** Fetches all URLs from Redis, check their publishing date and send the url to the [[Cleaner]]-Actor if this date was to long ago
    *
    */
  def sendEntriesToCleaner(keys : String): Unit ={
    /**
      * Fetches all keys (URLs) from Redis
      */
    val keyValue = redisConnection.keys(keys)

    keyValue match {
      case Some(map) =>
        map.foreach(
          url => {
            val pubDate = redisConnection.get[String](url.getOrElse("")).getOrElse("")
            val pubDateJoda = Formatter.formatStringToDateTime(pubDate).toLocalDate

            /**
              * If the pubDate is more than 7 days ago, the URL/Key will be send to the [[Cleaner]]-Actor
              */
            if(Days.daysBetween(pubDateJoda, DateTime.now().toLocalDate).getDays > 7)
              cleaner ! url.getOrElse("")

          }
        )
      case None =>
        logger.error(s"Key can not be parsed")
    }
  }

}
