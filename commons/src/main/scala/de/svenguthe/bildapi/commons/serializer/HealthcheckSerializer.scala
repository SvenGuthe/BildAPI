package de.svenguthe.bildapi.commons.serializer

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.util

import de.svenguthe.bildapi.commons.datatypes.HealthcheckMessage
import org.apache.kafka.common.serialization.Serializer

class HealthcheckSerializer extends Serializer[HealthcheckMessage]{

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

  }

  override def serialize(topic: String, data: HealthcheckMessage): Array[Byte] = {

    try {
      val byteOut = new ByteArrayOutputStream()
      val objOut = new ObjectOutputStream(byteOut)
      objOut.writeObject(data)
      objOut.close()
      byteOut.close()
      byteOut.toByteArray
    }
    catch {
      case ex:Exception => throw new Exception(ex.getMessage)
    }

  }

  override def close(): Unit = {

  }

}