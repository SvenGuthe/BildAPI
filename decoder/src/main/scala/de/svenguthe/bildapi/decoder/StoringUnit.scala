package de.svenguthe.bildapi.decoder

import akka.actor.Actor
import com.datastax.driver.core.Session
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.cassandra_interface.CassandraService
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.slf4j.LoggerFactory

class StoringUnit extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val cassandraService = CassandraService(conf)

  override def receive: Receive = {
    case (bildArticle : BildArticle, cassandraSession : Session) =>
      cassandraService.insertBildArticle(cassandraSession, bildArticle)
    case None =>
      logger.error("Decoder-Actor received non String-message")
  }

}
