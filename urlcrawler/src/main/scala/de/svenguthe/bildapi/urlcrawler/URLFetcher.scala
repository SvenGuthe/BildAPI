package de.svenguthe.bildapi.urlcrawler

import java.util.Date

import akka.actor.{Actor, Props}
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.{ActivityActorMessages, Actors, HealthcheckMessage, MessageStatus}
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable

object URLFetcher {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val startpages = List(conf.getString("urls.politics.startseite"),
    conf.getString("urls.news.startseite"),
    conf.getString("urls.geld.startseite"),
    conf.getString("urls.unterhaltung.startseite"),
    conf.getString("urls.sport.startseite"),
    conf.getString("urls.bundesliga.startseite"),
    conf.getString("urls.lifestyle.startseite"),
    conf.getString("urls.ratgeber.startseite"),
    conf.getString("urls.reise.startseite"),
    conf.getString("urls.auto.startseite"),
    conf.getString("urls.digital.startseite"),
    conf.getString("urls.spiele.startseite"),
    conf.getString("urls.regional.startseite"))

  private lazy val crawlerConfig = conf.getString("uRLCrawlerActorSystem.akka.actor.actors.urlCrawler")
  private lazy val activityTrackerConfig = conf.getString("activitytracker.actor.address")
  private lazy val className = this.getClass.getCanonicalName

}

/**
  * Fetches all the URLs from Redis and send them to the [[URLCrawler]]-Actor
  */
class URLFetcher extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)


  /**
    * Define the [[URLCrawler]]-Actor and the ActivityTracker Remote Actor
    */
  private lazy val crawler = context.actorOf(Props[URLCrawler], URLFetcher.crawlerConfig)
  private val actorSelection = context.actorSelection(URLFetcher.activityTrackerConfig)

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()

  private lazy val urlHashMap = mutable.HashMap[String, Date]()

  override def receive: Receive = {
    case msg: String =>
      msg match {
        case "initalizeFetching" =>
          val healthcheckMessage = HealthcheckMessage(
            Actors.URLCrawler,
            URLFetcher.className,
            MessageStatus.OK,
            ActivityActorMessages.LOG,
            value = msg,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
          redisURLToCrawler(redisConnection)
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier")
          val healthcheckMessage = HealthcheckMessage(
            Actors.URLCrawler,
            URLFetcher.className,
            MessageStatus.FAILURE,
            ActivityActorMessages.WRONGIDENTIFIER,
            value = wrongIdentifier,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
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
              val healthcheckMessage = HealthcheckMessage(
                Actors.URLCrawler,
                URLFetcher.className,
                MessageStatus.OK,
                ActivityActorMessages.INSERTED,
                value = url,
                timestamp = DateTime.now()
              )
              actorSelection ! healthcheckMessage
          }
        case "failure" =>
          logger.info(s"Delete key $url")
          redisConnection.del(url)
          val healthcheckMessage = HealthcheckMessage(
            Actors.URLCrawler,
            URLFetcher.className,
            MessageStatus.OK,
            ActivityActorMessages.DELETED,
            value = url,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
        case wrongIdentifier =>
          logger.error(s"URLFetcher received message with wrong identifier String: $wrongIdentifier and URL: $url")
          val healthcheckMessage = HealthcheckMessage(
            Actors.CLEANER,
            URLFetcher.className,
            MessageStatus.FAILURE,
            ActivityActorMessages.WRONGIDENTIFIER,
            value = wrongIdentifier,
            timestamp = DateTime.now()
          )
          actorSelection ! healthcheckMessage
      }
    case wrongFormat =>
      logger.error(s"URLFetcher received wrong message type: $wrongFormat")
      val healthcheckMessage = HealthcheckMessage(
        Actors.CLEANER,
        URLFetcher.className,
        MessageStatus.FAILURE,
        ActivityActorMessages.WRONGFORMAT,
        value = wrongFormat,
        timestamp = DateTime.now()
      )
      actorSelection ! healthcheckMessage
  }

  def redisURLToCrawler(redisConnection : RedisClient): Unit ={
    val keyValue = RedisService.getAllKeysFromRedis(redisConnection)
    logger.debug(s"All keys: ${keyValue.mkString(", ")}")

    URLFetcher.startpages.foreach(startpage => {
      crawler ! (startpage, DateTime.now().toDate)
    })

    keyValue match {
      case Some(map) =>
        map.foreach(
          url => {
            urlHashMap.put(url.getOrElse(""), DateTime.now().toDate)
          }
        )
      case None =>
    }

  }

}