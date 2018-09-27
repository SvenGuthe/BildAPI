package de.svenguthe.bildapi.cleaner

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

class OutdatedURLFetcher extends Actor {

  lazy val logger = LoggerFactory.getLogger(this.getClass)
  lazy val conf = ConfigFactory.load()

  private val cleaner = context.actorOf(Props[Cleaner], "Cleaner")

  lazy val redisConnection = RedisService.getRedisConnection

  override def receive: Receive = {
    case msg: String =>
      msg match {
        case "initalizeFetching" =>
          getEntryList
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
      }
    case wrongFormat =>
      logger.error(s"URLFetcher received wrong message type: $wrongFormat")
  }

  def getEntryList(): Unit ={
    val keyValue = redisConnection.keys("*")

    keyValue match {
      case Some(map) =>
        map.foreach(
          url => {
            cleaner ! (url.getOrElse(""), redisConnection.get[String](url.getOrElse("")).getOrElse(""))
          }
        )
      case None =>
    }
  }

}
