package de.svenguthe.bildapi.urlcrawler

import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.Formatter
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model._
import org.joda.time.{DateTime, Days}
import org.slf4j.LoggerFactory
import spray.json.JsValue
import spray.json._

import scala.util.matching.Regex

/** Object which provide functions to crawl a website */
object URLCrawlerService {


  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val listPre = List(conf.getString("urls.politics.pre"),
    conf.getString("urls.news.pre"),
    conf.getString("urls.geld.pre"),
    conf.getString("urls.unterhaltung.pre"),
    conf.getString("urls.sport.pre"),
    conf.getString("urls.bundesliga.pre"),
    conf.getString("urls.lifestyle.pre"),
    conf.getString("urls.ratgeber.pre"),
    conf.getString("urls.reise.pre"),
    conf.getString("urls.auto.pre"),
    conf.getString("urls.digital.pre"),
    conf.getString("urls.spiele.pre"),
    conf.getString("urls.regional.pre"))

  private lazy val bildPre = conf.getString("urls.bild.pre")

  /** Get all links from an website and send all the articles with pubdate < 7 days ago back to the sender
    *
    *  @param links All links/<a>-Elements which are found in a website
    *  @param sender the actor which sends the request [[URLFetcher]]
    *
    */
  def parsePage(links : ElementQuery[Element], sender : ActorRef): Unit ={
    links.foreach(
      link => {

        /**
          * Check if the <a> tag got any href Attributes
          */
        if (link.hasAttr("href")) {
          val href = link.attr("href")

          /**
            * Check if the href attribute is a link
            */
          if (listPre.exists(pre => href.startsWith(pre))) {

            val url = s"$bildPre$href"

            /**
              * Read also the fetched Page for checking the publishing Date
              */
            val browser = JsoupBrowser()
            val doc = browser.get(url)

            val html = doc.toHtml.toString
            /**
              * Searching for the pageTracking JavaScript Object with informations about the subChannels and IDs
              */
            val cmsDataPageTrackingRegex: Regex = "de.bild.cmsData.pageTracking[\\s\\S]*?(?=;)".r
            val cmsDataPageTracking = cmsDataPageTrackingRegex.findFirstIn(html)
            val fields : Map[String, JsValue] = cmsDataPageTracking match {
              case Some(cmsDataPageTrackingString) =>
                val jsonFromString = cmsDataPageTrackingString.replace("de.bild.cmsData.pageTracking = ", "").parseJson
                jsonFromString.asJsObject.fields
              case None =>
                logger.info("Nothing Found in Regex")
                Map[String, JsValue]()
            }

            val authorpubdate = fields.getOrElse("publicationDate", "").toString

            /**
              * If there is an AuthorPubDate it is normally an article
              */
            if(authorpubdate != "") {
              val jodatime = Formatter.formatStringToDateTimeCrawler(authorpubdate).toDate

              /**
                * If the Publishing date is more then 7 days ago, we don't fetch the url
                */
              if (Days.daysBetween(Formatter.formatStringToDateTimeCrawler(authorpubdate).withTimeAtStartOfDay, DateTime.now.withTimeAtStartOfDay).getDays < 7) {
                logger.info(s"Publish Page $url with PubTime $jodatime")
                sender ! ("publishURL", url, jodatime)
              } else {
                logger.info(s"Publish Date $jodatime of $url wasn't in the last week.")
              }

            } else {
              logger.info(s"Found Page $url with no PubTime")
              val jodatime = new DateTime(9999, 1, 1, 0, 0, 0, 0).toDate
              val links = doc >> "a"
              sender ! ("publishURL", url, jodatime)
            }
          }
        }
      }
    )
  }

  /** Extract all the links/<a>-Tags from the website
    *
    *  @param url The url of the Website
    *  @param sender the actor which sends the request [[URLFetcher]]
    *
    */
  def extractLinksFromURLandSendtoURLFetcher(url : String, sender : ActorRef) : Unit = {

    try {
      val browser = JsoupBrowser()
      val doc = browser.get(url)

      /**
        * Get all <a> tags from the document
        */
      val links = doc >> "a"
      parsePage(links, sender)

    } catch {
      case e: Exception =>
        logger.error(s"Error while connecting or parsing url $url: $e")
        sender ! ("failure", url, DateTime.now().toDate)
    }
  }

}
