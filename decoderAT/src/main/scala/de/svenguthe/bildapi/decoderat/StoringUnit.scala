package de.svenguthe.bildapi.decoderat

import akka.actor.Actor
import com.datastax.driver.core.Session
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.cassandra_interface.CassandraService
import de.svenguthe.bildapi.commons.datatypes.HealthcheckMessage
import org.slf4j.LoggerFactory

class StoringUnit extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val cassandraService = CassandraService(conf)

  override def receive: Receive = {
    case (healthcheckmessage : HealthcheckMessage, cassandraSession : Session) =>
      cassandraService.insertHealthcheckmessage(cassandraSession, healthcheckmessage)
    case None =>
      logger.error("Decoder-Actor received non String-message")
  }

}
