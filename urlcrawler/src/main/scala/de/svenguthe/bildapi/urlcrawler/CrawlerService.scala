package de.svenguthe.bildapi.urlcrawler

import akka.actor.ActorRef
import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

object CrawlerService {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val conf = ConfigFactory.load()

  def getLinks(url : String, sender : ActorRef) : Unit = {

    val listBuffer = ListBuffer[String]()

    try {
      val browser = JsoupBrowser()
      val doc = browser.get(url)

      val links = doc >> "a"
      links.foreach(
        link => {
          if (link.hasAttr("href")){
            val href = link.attr("href")
            if(href.startsWith(conf.getString("urls.politics.pre"))){
              sender ! ("publishURL", s"${conf.getString("urls.bild.pre")}$href")
            }
          }
        }
      )

    } catch {
      case e: Exception =>
        logger.error(s"Error while connecting or parsing url $url: $e")
        sender ! ("failure", url)
    }
  }

}
