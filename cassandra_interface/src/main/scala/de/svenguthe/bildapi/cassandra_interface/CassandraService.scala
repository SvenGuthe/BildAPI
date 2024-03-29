package de.svenguthe.bildapi.cassandra_interface

import java.util.UUID

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import com.datastax.driver.core.{Cluster, DataType, Session}
import com.typesafe.config.{Config, ConfigFactory}
import de.svenguthe.bildapi.commons.datatypes.{BildArticle, HealthcheckMessage}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

case class CassandraService(moduleConfig : Config) {

  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val conf = ConfigFactory.load()

  private lazy val contactPointConf = conf.getString("cassandra.contactpoint")
  private lazy val keyspaceConf = conf.getString("cassandra.keyspace")
  private lazy val tables = conf.getConfig("cassandra.tables")

  def getCassandraSession(contactPoint : String = contactPointConf): Session = {
    logger.info(s"Try to connect to cassandra at $contactPoint")
    val cluster = Cluster.builder().addContactPoint(contactPoint).build()
    val session = cluster.connect()
    session
  }

  def createKeyspaceIfNotExists(session : Session, keyspaceName : String = keyspaceConf): Unit = {

    val result = session.execute(s"CREATE KEYSPACE IF NOT EXISTS $keyspaceName WITH replication = {"
      + " 'class': 'SimpleStrategy', "
      + " 'replication_factor': '1' "
      + "};" )

    logger.info(s"Result of keyspace creation with name $keyspaceName: $result")
  }

  def createTable(session : Session,
                  tableName : String,
                  mapAttributes : Map[String, DataType],
                  clusterKey : Map[String, DataType],
                  primaryKey : Map[String, DataType],
                  keyspaceName : String = keyspaceConf): Unit = {

      val query = SchemaBuilder
        .createTable(keyspaceName, tableName)
        .ifNotExists()

    primaryKey.foreach(mapValues => {
      query.addPartitionKey(mapValues._1, mapValues._2)
    })

    clusterKey.foreach(mapValues => {
      query.addClusteringColumn(mapValues._1, mapValues._2)
    })

    mapAttributes.foreach(mapValues => {
      query.addColumn(mapValues._1, mapValues._2)
    })

    val result = session.execute(query.toString)

    logger.info(s"Result of table creation with name $tableName: $result")

  }

  def createHealthcheckMessagesTable(session : Session) : Unit = {

    val partitionKey = Map[String, DataType](
      ("uuid", DataType.uuid())
    )

    val clusterKey = Map[String, DataType](
      ("module", DataType.text()),
      ("status", DataType.text()),
      ("timestamp", DataType.timestamp())
    )

    val attributes = Map[String, DataType](
      ("classname", DataType.text()),
      ("action", DataType.text()),
      ("value", DataType.text())
    )

    createTable(session,
      tables.getString("healthcheckMessages.name"),
      attributes,
      clusterKey,
      partitionKey)

  }

  def createRawBildArticlesTable(session : Session): Unit = {

    val partitionKey = Map[String, DataType](
      ("uuid", DataType.uuid())
    )

    val clusterKey = Map[String, DataType](
      ("subChannel1", DataType.text()),
      ("subChannel2", DataType.text()),
      ("url", DataType.text()),
      ("pubDate", DataType.timestamp()),
      ("documentid", DataType.text())
    )

    val attributes = Map[String, DataType](
      ("subChannel3", DataType.text()),
      ("subChannel4", DataType.text()),
      ("kicker", DataType.text()),
      ("headline", DataType.text()),
      ("subhead", DataType.text()),
      ("text", DataType.text()),
      ("crossheadings", DataType.list(DataType.text())),
      ("keywords", DataType.list(DataType.text())),
      ("crawlerTime", DataType.timestamp())
    )

    createTable(session,
      tables.getString("rawBildArticles.name"),
      attributes,
      clusterKey,
      partitionKey)
  }

  def insertHealthcheckmessage(session : Session, healthcheckMessage : HealthcheckMessage) : Unit =  {

    val tableName = tables.getString("healthcheckMessages.name")
    val insert = QueryBuilder
      .insertInto(keyspaceConf, tableName)
      .value("uuid", UUID.randomUUID())
      .value("module", healthcheckMessage.module.toString)
      .value("classname", healthcheckMessage.classname.toString)
      .value("status", healthcheckMessage.status.toString)
      .value("action", healthcheckMessage.action.toString)
      .value("value", healthcheckMessage.value.toString)
      .value("timestamp", healthcheckMessage.timestamp.toString())

    logger.info(s"Query: ${insert.toString}")
    val result = session.execute(insert.toString)
    logger.info(s"Result of insertion in table $tableName: $result")

  }

  def insertBildArticle(session : Session, bildArticle : BildArticle): Unit ={

    val tableName = tables.getString("rawBildArticles.name")
    val insert = QueryBuilder
      .insertInto(keyspaceConf, tableName)
      .value("uuid", UUID.randomUUID())
      .value("url", bildArticle.url)
      .value("pubDate", bildArticle.pubDate.toString())
      .value("subChannel1", bildArticle.subChannel1)
      .value("subChannel2", bildArticle.subChannel2)
      .value("subChannel3", bildArticle.subChannel3)
      .value("subChannel4", bildArticle.subChannel4)
      .value("kicker", bildArticle.kicker)
      .value("headline", bildArticle.headline)
      .value("subhead", bildArticle.subhead)
      .value("text", bildArticle.text)
      .value("crossheadings", seqAsJavaList(bildArticle.crosshaedings))
      .value("documentid", bildArticle.documentid)
      .value("keywords", seqAsJavaList(bildArticle.keywords))
      .value("crawlerTime", bildArticle.crawlerTime.toString())

    logger.info(s"Query: ${insert.toString}")
    val result = session.execute(insert.toString)
    logger.info(s"Result of insertion in table $tableName: $result")
  }

}
