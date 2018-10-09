package de.svenguthe.bildapi.urlcrawler

import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model._
import org.joda.time.{DateTime, Days}
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory

object CrawlerService {

  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  def parsePage(links : ElementQuery[Element], sender : ActorRef): Unit ={
    links.foreach(
      link => {

        // Check if the <a> tag got any href Attributes
        if (link.hasAttr("href")) {
          val href = link.attr("href")

          // Check if the href attribute is a link from the politics section
          if (href.startsWith(conf.getString("urls.politics.pre"))) {

            val url = s"${conf.getString("urls.bild.pre")}$href"

            // Read also the fetched Page for checking the publishing Date
            val browser = JsoupBrowser()
            val doc = browser.get(url)
            var authorpubdate = doc >> elementList(".authors__pubdate")

            // If there is an AuthorPubDate it is normally an article
            if(authorpubdate.size > 0) {
              val pubdate = authorpubdate(0).attr("datetime")
              val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
              val jodatime = dtf.parseDateTime(pubdate).toDate

              // If the Publishing date is more then 7 days ago, we don't fetch the url
              if (Days.daysBetween(dtf.parseDateTime(pubdate).withTimeAtStartOfDay(), DateTime.now().withTimeAtStartOfDay()).getDays() < 7) {
                logger.info(s"Publish Page $url with PubTime $jodatime")
                sender ! ("publishURL", url, jodatime)
              } else {
                logger.info(s"Publish Date $jodatime of $url wasn't in the last week.")
              }

            } else {
              logger.info(s"Found Page $url with no PubTime")
              val jodatime = DateTime.now().plusYears(10).toDate
              val links = doc >> "a"
              sender ! ("publishURL", url, jodatime)
            }
          }
        }
      }
    )
  }

  def getLinks(url : String, sender : ActorRef) : Unit = {

    try {
      val browser = JsoupBrowser()
      val doc = browser.get(url)

      // Get all <a> tags from the document
      val links = doc >> "a"
      parsePage(links, sender)

    } catch {
      case e: Exception =>
        logger.error(s"Error while connecting or parsing url $url: $e")
        sender ! ("failure", url, DateTime.now().toDate)
    }
  }

}
