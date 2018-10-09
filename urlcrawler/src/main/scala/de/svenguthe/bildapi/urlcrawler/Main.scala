package de.svenguthe.bildapi.urlcrawler

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/** Main Object witch extends App and triggers the url crawler */
object Main extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val confActorSystem = conf.getString("akka.akkaSystem")
  private lazy val urlFetcherConfig = conf.getString("akka.actors.urlFetcher")

  logger.info(s"===============================")
  logger.info(s"===== Starting URLCrawler =====")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[URLFetcher]]
    */
  val actorSystem = ActorSystem(confActorSystem)
  val urlFetcherActor = actorSystem.actorOf(Props[URLFetcher], urlFetcherConfig)

  import actorSystem.dispatcher

  /**
    * Crete a Scheduler which triggers the [[URLFetcher]] every 10 minutes
    */
  val schedule = actorSystem.scheduler.schedule(
      0 milliseconds,
      10 minutes,
    urlFetcherActor,
      "initalizeFetching")

}
