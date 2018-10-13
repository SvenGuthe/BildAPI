package de.svenguthe.bildapi.crawler

import java.util.Properties

import akka.actor.Actor
import de.svenguthe.bildapi.commons.datatypes.BildArticle

object KafkaProducer {

  val  props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

}

class KafkaProducer extends Actor {

  override def receive: Receive = {
    case bildArticle: BildArticle =>

    case None =>

  }

}
