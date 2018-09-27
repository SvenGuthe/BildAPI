package de.svenguthe.bildapi.cleaner

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object Main extends App {

  lazy val logger = LoggerFactory.getLogger(this.getClass)
  lazy val conf = ConfigFactory.load()

  val confActorSystem = conf.getString("akka.akkaSystem")

  logger.info(s"===============================")
  logger.info(s"======= Starting Cleaner ======")
  logger.info(s"===============================")

  // Initialize ActorSystem
  val actorSystem = ActorSystem(confActorSystem)
  val outdatedURLFetcherActor = actorSystem.actorOf(Props[OutdatedURLFetcher], "OutdatedURLFetcher")

  import actorSystem.dispatcher

  val schedule = actorSystem.scheduler.schedule(
    0 milliseconds,
    1 days,
    outdatedURLFetcherActor,
    "initalizeFetching")

}
