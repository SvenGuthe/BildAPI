package de.svenguthe.bildapi.activitytracker

import java.util.{Properties, UUID}

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.HealthcheckMessage
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.LoggerFactory

object KafkaProducerActor {

  /**
    * Factories to load the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val healthcheckmessages = conf.getString("kafka.topics.healthcheckmessages")

  private lazy val bootstrapServers = s"${conf.getString("kafka.bootstrap.servers.hostname")}:${conf.getInt("kafka.bootstrap.servers.port")}"

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", conf.getString("kafka.serializers.healthcheckmessage.key.keySerializer"))
  props.put("value.serializer", conf.getString("kafka.serializers.healthcheckmessage.value.valueSerializer"))

  private lazy val producerHealthcheckmessage = new KafkaProducer[String, HealthcheckMessage](props)

  logger.info(s"Kafka Producer Settings: $producerHealthcheckmessage")

}

class KafkaProducerActor extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  override def receive: Receive = {
    case healthcheckMessage: HealthcheckMessage =>
      val record = new ProducerRecord(KafkaProducerActor.healthcheckmessages, UUID.randomUUID().toString, healthcheckMessage)
      logger.info(s"KafkaProducer sends Healthcheckmessage to Kafka: $healthcheckMessage")
      KafkaProducerActor.producerHealthcheckmessage.send(record)
    case initialMessage : String if initialMessage == "Initialize ActivityTracker Actor" =>
      logger.info("KafkaProducerActor initialized successful")
    case _ =>
      logger.error("KafkaProducer received no Healthcheckmessage")
  }

}
