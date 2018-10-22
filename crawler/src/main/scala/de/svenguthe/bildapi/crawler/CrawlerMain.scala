package de.svenguthe.bildapi.crawler

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object CrawlerMain extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val confActorSystem = conf.getString("crawlerSystem.akka.systemName")
  private lazy val crawlerConfig = conf.getString("crawlerSystem.akka.actor.actors.crawler")
  private lazy val crawlerSystem  = conf.getConfig("crawlerSystem")


  logger.info(s"===============================")
  logger.info(s"======= Starting Crawler ======")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[Crawler]]
    */
  val actorSystem = ActorSystem(confActorSystem, crawlerSystem)
  val crawlerActor = actorSystem.actorOf(Props[Crawler], crawlerConfig)

  import actorSystem.dispatcher

  /**
    * Crete a Scheduler which triggers the [[Crawler]] every 10 minutes
    */
  val schedule = actorSystem.scheduler.schedule(
    0 milliseconds,
    10 minutes,
    crawlerActor,
    "startCrawling")

}
