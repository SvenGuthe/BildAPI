package de.svenguthe.bildapi.decoder

import akka.actor.Actor
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.slf4j.LoggerFactory

class StoringUnit extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  override def receive: Receive = {
    case bildArticle : BildArticle =>

    case None =>
      logger.error("Decoder-Actor received non String-message")
  }

}
