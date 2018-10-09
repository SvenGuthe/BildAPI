import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.jsoup.HttpStatusException
import org.slf4j.LoggerFactory

class Downloader extends Actor {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  override def receive: Receive = {

    case (message: String, url: String) =>
      message match {
        case "downloadHTMLfromURL" =>
          val browser = JsoupBrowser()
          try {
            val doc = browser.get(url)
            // ToDo: Send doc to filter
          } catch {
            case httpStatusException : HttpStatusException =>
              logger.error(s"HTTP-Error while establish HTTP Connection to ${httpStatusException.getUrl} " +
                s"with Status-Code: ${httpStatusException.getStatusCode}")
            case e : Exception =>
              logger.error(s"Error while establish HTTP Connection to $url: $e")
          }
        case unknownMessage =>
          logger.error(s"Downloader-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Downloader-Actor received non String-message")
  }

}
