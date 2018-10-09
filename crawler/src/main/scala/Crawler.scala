import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import de.svenguthe.bildapi.redisinterface.RedisService
import org.slf4j.LoggerFactory

class Crawler extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val downloaderConfig = conf.getString("akka.actors.downloader")

  /**
    * Define the [[Downloader]]-Actor
    */
  private lazy val downloader = context.actorOf(Props[Downloader], downloaderConfig)

  /**
    * Establish a redis database connection at the first time it is called
    */
  private lazy val redisConnection = RedisService.getRedisConnection()

  override def receive: Receive = {
    case message : String =>
      message match {
        case "startCrawling" =>
          logger.info("Crawler-Actor received a message to start crawling")
          val allKeysFromRedis = RedisService.getAllKeysFromRedis(redisConnection)

          try {
            allKeysFromRedis.get.foreach(url => {

              val pubDate = redisConnection.get[String](url.getOrElse("")).getOrElse("")
              val pubDateJoda = Formatter.formatStringToDateTime(pubDate).withTimeAtStartOfDay()

              if(pubDateJoda.year.get() == 9999){
                logger.info(s"Try to crawl no article at ${url.getOrElse("")}")
              } else{
                val urlString = url.get
                logger.info(s"Crawl Article at $urlString")
                downloader ! ("downloadHTMLfromURL", urlString)
              }

            })
          } catch {
            case e : Exception => logger.error(s"Error while reading keys from redis: $e")
          }

        case unknownMessage =>
          logger.error(s"Crawler-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Crawler-Actor received non String-message")
  }

}
