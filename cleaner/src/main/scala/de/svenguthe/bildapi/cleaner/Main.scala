package de.svenguthe.bildapi.cleaner

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/** Main Object witch extends App and triggers the cleaning process of the redis database */
object Main extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  lazy val logger = LoggerFactory.getLogger(this.getClass)
  lazy val conf = ConfigFactory.load()

  lazy val confActorSystem = conf.getString("akka.akkaSystem")
  lazy val outdatedURLFetcherConfig = conf.getString("akka.actors.outdatedURLFetcher")

  logger.info(s"===============================")
  logger.info(s"======= Starting Cleaner ======")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[OutdatedURLFetcher]]
    */
  val actorSystem = ActorSystem(confActorSystem)
  val outdatedURLFetcherActor = actorSystem.actorOf(Props[OutdatedURLFetcher], outdatedURLFetcherConfig)

  import actorSystem.dispatcher

  /**
    * Crete a Scheduler which triggers the OutdatedURLFetcher every day
    */
  val schedule = actorSystem.scheduler.schedule(
    0 milliseconds,
    1 days,
    outdatedURLFetcherActor,
    "startCleaning")

}
