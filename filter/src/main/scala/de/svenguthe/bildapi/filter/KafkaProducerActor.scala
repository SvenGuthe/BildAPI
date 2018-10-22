package de.svenguthe.bildapi.filter

import java.util.{Properties, UUID}

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.LoggerFactory

object KafkaProducerActor {

  /**
    * Factories to load the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val success = conf.getString("kafka.topics.success")

  private lazy val bootstrapServers = s"${conf.getString("kafka.bootstrap.servers.hostname")}:${conf.getInt("kafka.bootstrap.servers.port")}"

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", conf.getString("kafka.serializers.bildarticle.key.keySerializer"))
  props.put("value.serializer", conf.getString("kafka.serializers.bildarticle.value.valueSerializer"))

  private lazy val producerSuccessful = new KafkaProducer[String, BildArticle](props)

  logger.info(s"Kafka Producer Settings: $producerSuccessful")

}

class KafkaProducerActor extends Actor {

  /**
    * Factories to load the logger
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  override def receive: Receive = {
    case bildArticle: BildArticle =>
      val record = new ProducerRecord(KafkaProducerActor.success, UUID.randomUUID().toString, bildArticle)
      logger.info("KafkaProducer sends BildArticle to Kafka")
      KafkaProducerActor.producerSuccessful.send(record)
    case _ =>
      logger.error("KafkaProducer received no BildArticle")
  }

}
