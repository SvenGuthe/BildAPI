package de.svenguthe.bildapi.crawler

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val confActorSystem = conf.getString("filterActorSystem.akka.systemName")
  private lazy val filterConfig = conf.getString("filterActorSystem.akka.actor.actors.filter")
  private lazy val filterSystem  = conf.getConfig("filterActorSystem")

  logger.info(s"===============================")
  logger.info(s"======= Starting Filter =======")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[Filter]]
    */
  val actorSystem = ActorSystem(confActorSystem, filterSystem)
  val urlFetcherActor = actorSystem.actorOf(Props[Filter], filterConfig)

  urlFetcherActor ! "Initialize FilterActor"

}
