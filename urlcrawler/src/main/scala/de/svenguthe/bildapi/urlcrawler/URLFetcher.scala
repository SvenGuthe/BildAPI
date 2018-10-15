package de.svenguthe.bildapi.urlcrawler

import java.util.Date

import akka.actor.{Actor, Props}
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.redisinterface.RedisService
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap

object URLFetcher {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val politicsStartseite = conf.getString("urls.politics.startseite")
  private lazy val newsStartseite = conf.getString("urls.news.startseite")
  private lazy val geldStartseite = conf.getString("urls.geld.startseite")
  private lazy val unterhaltungStartseite = conf.getString("urls.unterhaltung.startseite")
  private lazy val sportStartseite = conf.getString("urls.sport.startseite")
  private lazy val bundesligaStartseite = conf.getString("urls.bundesliga.startseite")
  private lazy val lifestyleStartseite = conf.getString("urls.lifestyle.startseite")
  private lazy val ratgeberStartseite = conf.getString("urls.ratgeber.startseite")
  private lazy val reiseStartseite = conf.getString("urls.reise.startseite")
  private lazy val autoStartseite = conf.getString("urls.auto.startseite")
  private lazy val digitalStartseite = conf.getString("urls.digital.startseite")
  private lazy val spieleStartseite = conf.getString("urls.spiele.startseite")
  private lazy val regionalStartseite = conf.getString("urls.regional.startseite")
  
  val startpages = List(politicsStartseite,
    newsStartseite,
    geldStartseite,
    unterhaltungStartseite,
    sportStartseite,
    bundesligaStartseite,
    lifestyleStartseite,
    ratgeberStartseite,
    reiseStartseite,
    autoStartseite,
    digitalStartseite,
    spieleStartseite,
    regionalStartseite)

}

/**
  * Fetches all the URLs from Redis and send them to the [[URLCrawler]]-Actor
  */
class URLFetcher extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val crawlerConfig = conf.getString("akka.actors.urlCrawler")


  /**
    * Define the [[URLCrawler]]-Actor
    */
  private lazy val crawler = context.actorOf(Props[URLCrawler], crawlerConfig)

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()

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
            // crawler ! (url.getOrElse(""), DateTime.now().toDate)
          }
        )
      case None =>
    }

  }

}