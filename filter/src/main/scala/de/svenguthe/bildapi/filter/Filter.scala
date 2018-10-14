package de.svenguthe.bildapi.filter

import akka.actor.{Actor, Props}
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.util.matching.Regex
import spray.json._

class Filter extends Actor {

  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val crawler = context.actorOf(Props[KafkaProducerActor], "KafkaProducerActor")

  override def receive: Receive = {
    case (message: String, url: String, document: String, pubDate: DateTime, crawlerTime : DateTime) =>
      message match {
        case "Document" =>
          logger.info("Get HTML Document as String")
          logger.info(s"url: $url, pubDate: $pubDate")
          val browser = JsoupBrowser()
          try {
            val documentAsDoc = browser.parseString(document)

            /**
              * Searching for the kicker, headline subhead and the raw text in the HTML document
              */
            val bildArticle_kicker = documentAsDoc >> element("article header h1 .kicker") >> text
            val bildArticle_headline = documentAsDoc >> element("article header h1 .headline") >> text
            val bildArticle_subhead = documentAsDoc >> element("article header .subhead") >> text
            val articleText = documentAsDoc >> elementList("article .txt p")

            /**
              * Concatenate all the text-parts to a single String
              */
            val textAsList = articleText.map(textPart => {
              textPart >> text
            })
            val bildArticle_text = textAsList.mkString(" ")

            /**
              * Build a List of Crossheadings from the HTML document
              */
            val articleCrossheadings = documentAsDoc >> elementList("article .txt .crossheading")
            val bildArticle_crossheadings = articleCrossheadings.map(crossheading => {
              crossheading >> text
            })

            /**
              * Searching for the pageTracking JavaScript Object with informations about the subChannels and IDs
              */
            val cmsDataPageTrackingRegex: Regex = "de.bild.cmsData.pageTracking[\\s\\S]*?(?=;)".r
            val cmsDataPageTracking = cmsDataPageTrackingRegex.findFirstIn(document)
            val fields : Map[String, JsValue] = cmsDataPageTracking match {
              case Some(cmsDataPageTrackingString) =>
                val jsonFromString = cmsDataPageTrackingString.replace("de.bild.cmsData.pageTracking = ", "").parseJson
                jsonFromString.asJsObject.fields
              case None =>
                logger.info("Nothing Found in Regex")
                Map[String, JsValue]()
            }

            /**
              * Format for data type [[de.svenguthe.bildapi.commons.datatypes.BildArticle]]
              */

            val bildArticle_url = url
            val bildArticle_pubDate = pubDate
            val bildArticle_subChannel1 = fields.getOrElse("subChannel1", "").toString
            val bildArticle_subChannel2 = fields.getOrElse("subChannel2", "").toString
            val bildArticle_subChannel3 = fields.getOrElse("subChannel3", "").toString
            val bildArticle_subChannel4 = fields.getOrElse("subChannel4", "").toString
            val bildArticle_documentid = fields.getOrElse("documentId", "").toString
            val bildArticle_keywords = fields.getOrElse("keywords", "").toString.split(",").toList

            val bildArticle = BildArticle(
              url = bildArticle_url,
              pubDate = bildArticle_pubDate,
              subChannel1 = bildArticle_subChannel1,
              subChannel2 = bildArticle_subChannel2,
              subChannel3 = bildArticle_subChannel3,
              subChannel4 = bildArticle_subChannel4,
              kicker = bildArticle_kicker,
              headline = bildArticle_headline,
              subhead = bildArticle_subhead,
              text = bildArticle_text,
              crosshaedings = bildArticle_crossheadings,
              documentid = bildArticle_documentid,
              keywords = bildArticle_keywords,
              crawlerTime = crawlerTime
            )

            logger.info(s"$bildArticle")
            crawler ! bildArticle

          } catch {
            case e : Exception => logger.error(s"Error while parsing HTML: $e")
          }
        case unknownMessage =>
          logger.error(s"Downloader-Actor received a unknown String-message: $unknownMessage")
      }
    case message: String =>
      message match {
        case "Initialize FilterActor" =>
          logger.info("Filter-Initialization successful")
        case unknownMessage =>
          logger.error(s"Downloader-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Downloader-Actor received non String-message")
  }

}
