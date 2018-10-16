package de.svenguthe.bildapi.decoder

import java.time.Duration
import java.util.Properties

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import de.svenguthe.bildapi.cassandra_interface.CassandraService
import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.util


object PollLoop extends App {

  /**
    * Factories to load the logger and the typesafe-configuration
    */
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  logger.info(s"===============================")
  logger.info(s"======= Starting Decoder ======")
  logger.info(s"===============================")

  private lazy val confActorSystem = conf.getString("decoderSystem.akka.systemName")
  private lazy val storingConfig = conf.getString("decoderSystem.akka.actor.actors.storingUnit")
  private lazy val decoderSystem  = conf.getConfig("decoderSystem")

  /**
    * Initialize Cassandra
    */
  private val cassandraService = CassandraService(conf)
  private val cassandraSession = cassandraService.getCassandraSession()
  cassandraService.createKeyspaceIfNotExists(cassandraSession)
  cassandraService.createRawBildArticlesTable(cassandraSession)

  /**
    * Initialize the Actor System and define the [[StoringUnit]]
    */
  val actorSystem = ActorSystem(confActorSystem, decoderSystem)
  val storingActor = actorSystem.actorOf(Props[StoringUnit], storingConfig)

  private lazy val success = conf.getString("kafka.topics.success")

  private lazy val bootstrapServers = s"${conf.getString("kafka.bootstrap.servers.hostname")}:${conf.getInt("kafka.bootstrap.servers.port")}"

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.deserializer", conf.getString("kafka.deserializers.key.keyDeserializer"))
  props.put("value.deserializer", conf.getString("kafka.deserializers.value.valueDeserializer"))
  props.put("group.id", conf.getString("kafka.group.id.decoder"))
  props.put("auto.offset.reset", conf.getString("kafka.auto.offset.reset"))
  props.put("enable.auto.commit", conf.getString("kafka.enable.auto.commit"))

  private lazy val duration = Duration.ofMinutes(10)

  val consumerSuccessful = new KafkaConsumer[String, BildArticle](props)
  consumerSuccessful.subscribe(util.Collections.singletonList(success))

  logger.info(s"Kafka Consumer Settings: $consumerSuccessful")

  try {
    while (true) {
      val records = consumerSuccessful.poll(duration)
      records.forEach(recods => {
        val bildArticle = recods.value()
        logger.info(s"Consume Bild article from kafka: $bildArticle")
        storingActor ! (bildArticle, cassandraSession)
      })
      consumerSuccessful.commitSync()
    }
  }
  finally {
    consumerSuccessful.close()
    }

}