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

/** Object which provide functions to crawl a website */
object URLCrawlerService {


  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val politicsPre = conf.getString("urls.politics.pre")
  private lazy val newsPre = conf.getString("urls.news.pre")
  private lazy val geldPre = conf.getString("urls.geld.pre")
  private lazy val unterhaltungPre = conf.getString("urls.unterhaltung.pre")
  private lazy val sportPre = conf.getString("urls.sport.pre")
  private lazy val lifestylePre = conf.getString("urls.lifestyle.pre")
  private lazy val ratgeberPre = conf.getString("urls.ratgeber.pre")
  private lazy val reisePre = conf.getString("urls.reise.pre")
  private lazy val autoPre = conf.getString("urls.auto.pre")
  private lazy val digitalPre = conf.getString("urls.digital.pre")
  private lazy val spielePre = conf.getString("urls.spiele.pre")
  private lazy val regionalPre = conf.getString("urls.regional.pre")

  private lazy val listPre = List(politicsPre,
    newsPre,
    geldPre,
    unterhaltungPre,
    sportPre,
    lifestylePre,
    ratgeberPre,
    reisePre,
    autoPre,
    digitalPre,
    spielePre,
    regionalPre)

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
            var authorpubdate = doc >> elementList(".authors__pubdate")

            /**
              * If there is an AuthorPubDate it is normally an article
              */
            if(authorpubdate.nonEmpty) {
              val pubdate = authorpubdate.head.attr("datetime")
              val jodatime = Formatter.formatStringToDateTimeCrawler(pubdate).toDate

              /**
                * If the Publishing date is more then 7 days ago, we don't fetch the url
                */
              if (Days.daysBetween(Formatter.formatStringToDateTimeCrawler(pubdate).withTimeAtStartOfDay, DateTime.now.withTimeAtStartOfDay).getDays < 7) {
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
