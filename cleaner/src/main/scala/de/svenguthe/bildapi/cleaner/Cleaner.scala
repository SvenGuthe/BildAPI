package de.svenguthe.bildapi.cleaner

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.{ActivityActorMessages, Actors, HealthcheckMessage, MessageStatus}
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

object Cleaner {

  private lazy val conf = ConfigFactory.load()
  private lazy val activityTrackerActor = conf.getString("activitytracker.actor.address")
  private lazy val className = this.getClass.getCanonicalName;

}

/**
  * The [[Cleaner]] is an Actor which deletes all the received messages from Redis
  */
class Cleaner extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val actorSelection = context.actorSelection(Cleaner.activityTrackerActor)


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
      val healthcheckMessage = HealthcheckMessage(
        Actors.CLEANER,
        Cleaner.className,
        MessageStatus.OK,
        ActivityActorMessages.DELETED,
        value = url,
        timestamp = DateTime.now()
      )
      actorSelection ! healthcheckMessage


    /**
      * If the Message is in an other format, this cannot be parsed
      */
    case wrongFormat =>
      logger.error(s"Cleaner received wrong message type: $wrongFormat")
      val healthcheckMessage = HealthcheckMessage(
        Actors.CLEANER,
        Cleaner.className,
        MessageStatus.FAILURE,
        ActivityActorMessages.WRONGFORMAT,
        value = wrongFormat,
        timestamp = DateTime.now()
      )
      actorSelection ! healthcheckMessage
  }

}
