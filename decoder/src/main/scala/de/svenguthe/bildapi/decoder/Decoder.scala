package de.svenguthe.bildapi.decoder

import java.time.Duration
import java.util.Properties

import akka.actor.{Actor, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.util

import scala.collection.JavaConverters._

// ToDo: Have to be a Kafka Streaming Consumer

object Decoder {

  /**
    * Factories to load the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val success = conf.getString("kafka.topics.success")

  private lazy val bootstrapServers = s"${conf.getString("kafka.bootstrap.servers.hostname")}:${conf.getInt("kafka.bootstrap.servers.port")}"

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.deserializer", conf.getString("kafka.deserializers.key.keyDeserializer"))
  props.put("value.deserializer", conf.getString("kafka.deserializers.value.valueDeserializer"))
  props.put("group.id", conf.getString("kafka.group.id.decoder"))
  props.put("auto.offset.reset", conf.getString("kafka.auto.offset.reset"))
  props.put("enable.auto.commit", conf.getString("kafka.enable.auto.commit"))

  private lazy val duration = Duration.ofMillis(10000)

  private val consumerSuccessful = new KafkaConsumer[String, BildArticle](props)

  logger.info(s"Kafka Consumer Settings: $consumerSuccessful")

  private lazy val storingUnitConfig = conf.getString("decoderSystem.akka.actor.actors.storingUnit")


}

class Decoder extends Actor {

  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  /**
    * Define the [[StoringUnit]]-Actor
    */
  private lazy val storingUnit = context.actorOf(Props[StoringUnit], Decoder.storingUnitConfig)

  override def receive: Receive = {
    case message : String =>
      message match {
        case "startConsuming" =>
          logger.info("Start consuming Messages from Kafka")
          Decoder.consumerSuccessful.wakeup()
          Decoder.consumerSuccessful.subscribe(util.Collections.singletonList(Decoder.success))

          val records = Decoder.consumerSuccessful.poll(Decoder.duration).asScala
          records.foreach(consumerRecord => {
            val bildArticle = consumerRecord.value()
            logger.info(s"Consume Bild article from kafka: $bildArticle")
            storingUnit ! bildArticle
          })
          Decoder.consumerSuccessful.close()

        case unknownMessage =>
          logger.error(s"Decoder-Actor received a unknown String-message: $unknownMessage")
      }
    case None =>
      logger.error("Decoder-Actor received non String-message")
  }

}
