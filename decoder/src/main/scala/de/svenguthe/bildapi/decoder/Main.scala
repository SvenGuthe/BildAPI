package de.svenguthe.bildapi.decoder

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object Main extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  logger.info(s"===============================")
  logger.info(s"======= Starting Decoder ======")
  logger.info(s"===============================")

  private lazy val confActorSystem = conf.getString("decoderSystem.akka.systemName")
  private lazy val decoderConfig = conf.getString("decoderSystem.akka.actor.actors.decoder")
  private lazy val decoderSystem  = conf.getConfig("decoderSystem")

  /**
    * Initialize the Actor System and define the [[Decoder]]
    */
  val actorSystem = ActorSystem(confActorSystem, decoderSystem)
  val decoderActor = actorSystem.actorOf(Props[Decoder], decoderConfig)

  import actorSystem.dispatcher

  /**
    * Crete a Scheduler which triggers the [[Decoder]] every 10 minutes
    */
  val schedule = actorSystem.scheduler.schedule(
    0 milliseconds,
    10 minutes,
    decoderActor,
    "startConsuming")

}
