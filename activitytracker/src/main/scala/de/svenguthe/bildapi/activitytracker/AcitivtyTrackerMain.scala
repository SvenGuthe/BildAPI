package de.svenguthe.bildapi.activitytracker

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object AcitivtyTrackerMain extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val confActorSystem = conf.getString("activityTrackerSystem.akka.systemName")
  private lazy val activityTrackerConfig = conf.getString("activityTrackerSystem.akka.actor.actors.kafkaProducerActor")
  private lazy val activityTrackerSystem  = conf.getConfig("activityTrackerSystem")

  logger.info(s"===============================")
  logger.info(s"== Starting Activity Tracker ==")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[KafkaProducerActor]]
    */
  val actorSystem = ActorSystem(confActorSystem, activityTrackerSystem)
  val activityTrackerActor = actorSystem.actorOf(Props[KafkaProducerActor], activityTrackerConfig)

  activityTrackerActor ! "Initialize ActivityTracker Actor"

}