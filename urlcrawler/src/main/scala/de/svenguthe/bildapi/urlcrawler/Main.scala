package de.svenguthe.bildapi.urlcrawler

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object Main extends App {

  val logger = LoggerFactory.getLogger(this.getClass)

  val conf = ConfigFactory.load()
  val startpage = conf.getString("urls.politics.startpage")

  logger.info(s"===============================")
  logger.info(s"===== Starting URLCrawler =====")
  logger.info(s"===============================")

  logger.info(s"  Configuration")
  logger.info(s"    Startpage: $startpage")

  // Initialize ActorSystem
  val actorSystem = ActorSystem("crawlerActorSystem")
  val urlFetcherActor = actorSystem.actorOf(Props[URLFetcher], "URLFetcher")

  import actorSystem.dispatcher

  val schedule = actorSystem.scheduler.schedule(
      0 milliseconds,
      10 seconds,
    urlFetcherActor,
      "Test")

}
