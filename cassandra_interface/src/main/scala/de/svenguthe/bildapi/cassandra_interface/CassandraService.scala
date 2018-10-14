package de.svenguthe.bildapi.cassandra_interface

import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

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
    val result = session.execute(query)
    logger.info(s"Result of keyspace creation with name $keyspaceName: $result")
  }

  def createTable(session : Session,
                  tableName : String,
                  mapAttributes : Map[String, String],
                  primaryKey : String,
                  keyspaceName : String = keyspaceConf): Unit = {

    mapAttributes.get(primaryKey) match {
      case Some(primaryKeyFound) =>
        val stringBuilder = new StringBuilder()
        stringBuilder ++= s"CREATE TABLE IF NOT EXISTS $keyspaceName.$tableName("
        mapAttributes.foreach(mapValues => {
          stringBuilder ++= s"${mapValues._1} ${mapValues._2}"
          stringBuilder ++= ", "
        })
        stringBuilder ++= s"PRIMARY KEY($primaryKeyFound));"
        val query = stringBuilder.mkString
        val result = session.execute(query)
        logger.info(s"Result of table creation with name $tableName: $result")

      case None =>
        logger.info(s"Primary Key $primaryKey is no attribute")
    }

  }

  def createRawBildArticlesTable(session : Session): Unit = {
    val attributes = Map[String, String](
      ("test", "int")
    )
    createTable(session,
      tables.getString("rawBildArticles.name"),
      attributes,
      "test")
  }

}
