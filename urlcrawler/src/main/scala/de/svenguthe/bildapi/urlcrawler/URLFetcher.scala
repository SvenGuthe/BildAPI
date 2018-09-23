package de.svenguthe.bildapi.urlcrawler

import akka.actor.{Actor, Props}

class URLFetcher extends Actor {

  def receive = {
    case msg: String => ???
  }

  def urlToCrawler(urlList: Seq[String]): Unit ={
    urlList.foreach(
      url => {
        val crawler = context.actorOf(Props[Crawler], "Crawler")
        crawler ! url
      }
    )
  }

}