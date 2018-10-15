package de.svenguthe.bildapi.cassandra_interface

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.config.{Config, ConfigFactory}
import de.svenguthe.bildapi.commons.datatypes.BildArticle
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
    val query = s"CREATE KEYSPACE IF NOT EXISTS $keyspaceName " +
      s"WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};"
    logger.info(s"Query: $query")
    val result = session.execute(query)
    logger.info(s"Result of keyspace creation with name $keyspaceName: $result")
  }

  def createTable(session : Session,
                  tableName : String,
                  mapAttributes : Map[String, String],
                  primaryKey : List[String],
                  keyspaceName : String = keyspaceConf): Unit = {

      val stringBuilder = new StringBuilder()
      stringBuilder ++= s"CREATE TABLE IF NOT EXISTS $keyspaceName.$tableName("
      mapAttributes.foreach(mapValues => {
        stringBuilder ++= s"${mapValues._1} ${mapValues._2}"
        stringBuilder ++= ", "
      })
      stringBuilder ++= s"PRIMARY KEY(${primaryKey.mkString(", ")}));"
      val query = stringBuilder.mkString
      logger.info(s"Query: $query")
      val result = session.execute(query)
      logger.info(s"Result of table creation with name $tableName: $result")

  }

  def createRawBildArticlesTable(session : Session): Unit = {
    val attributes = Map[String, String](
      ("url", "text"),
      ("pubDate", "timestamp"),
      ("subChannel1", "text"),
      ("subChannel2", "text"),
      ("subChannel3", "text"),
      ("subChannel4", "text"),
      ("kicker", "text"),
      ("headline", "text"),
      ("subhead", "text"),
      ("text", "text"),
      ("crossheadings", "list<text>"),
      ("documentid", "text"),
      ("keywords", "list<text>"),
      ("crawlerTime", "timestamp")
    )
    createTable(session,
      tables.getString("rawBildArticles.name"),
      attributes,
      List("documentid", "crawlerTime"))
  }

  def insertBildArticle(session : Session, bildArticle : BildArticle): Unit ={

    val tableName = tables.getString("rawBildArticles.name")
    val insert = QueryBuilder
      .insertInto(keyspaceConf, tableName)
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
