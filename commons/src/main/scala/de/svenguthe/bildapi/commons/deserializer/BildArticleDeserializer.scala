package de.svenguthe.bildapi.commons.deserializer

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.util

import de.svenguthe.bildapi.commons.datatypes.BildArticle
import org.apache.kafka.common.serialization.Deserializer

class BildArticleDeserializer extends Deserializer[BildArticle] {

  override def configure(configs: util.Map[String,_],isKey: Boolean):Unit = {

  }
  override def deserialize(topic:String,bytes: Array[Byte]) = {
    val byteIn = new ByteArrayInputStream(bytes)
    val objIn = new ObjectInputStream(byteIn)
    val obj = objIn.readObject().asInstanceOf[BildArticle]
    byteIn.close()
    objIn.close()
    obj
  }
  override def close():Unit = {

  }

}
