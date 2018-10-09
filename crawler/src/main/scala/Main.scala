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

  private lazy val confActorSystem = conf.getString("akka.akkaSystem")
  private lazy val crawlerConfig = conf.getString("akka.actors.crawler")

  logger.info(s"===============================")
  logger.info(s"======= Starting Crawler ======")
  logger.info(s"===============================")

  /**
    * Initialize the Actor System and define the [[Crawler]]
    */
  val actorSystem = ActorSystem(confActorSystem)
  val urlFetcherActor = actorSystem.actorOf(Props[Crawler], crawlerConfig)

  import actorSystem.dispatcher

  /**
    * Crete a Scheduler which triggers the [[Crawler]] every 10 minutes
    */
  val schedule = actorSystem.scheduler.schedule(
    0 milliseconds,
    10 minutes,
    urlFetcherActor,
    "startCrawling")

}
