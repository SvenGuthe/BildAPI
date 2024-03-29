package de.svenguthe.bildapi.redisinterface

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

object RedisService {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val conf = ConfigFactory.load()
  private val redisHost = conf.getString("redis.host")
  private val redisPort = conf.getInt("redis.port")
  private val startpage = conf.getString("urls.startpage")

  def getRedisConnection() : RedisClient = {
    val redisService = new RedisService()
    val redisConnection = redisService.connectToRedis()

    redisConnection.get[String](startpage.toString) match {
      case Some(exists) => logger.info(s"Redis allready initialized: ${startpage.toString} - $exists")
      case _ =>
        logger.info("Initialize Redis")
        redisConnection.set(startpage.toString, new DateTime(9999, 1, 1, 0, 0, 0, 0).toDate)
    }

    redisConnection
  }

  def getAllKeysFromRedis(redisClient : RedisClient,
                          keys : String = "*") : Option[List[Option[String]]] = {
    redisClient.keys(keys)
  }

}

class RedisService() {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def connectToRedis() : RedisClient = {
    connectToRedis(RedisService.redisHost, RedisService.redisPort)
  }

  def connectToRedis(redisHost : String, redisPort : Int) : RedisClient = {
    try{
      logger.info(s"Try to connect to redis: $redisHost:$redisPort")
      new RedisClient(redisHost, redisPort)
    } catch {
      case e: Exception =>
        logger.error(s"Error in establish a connection to redis: $redisHost:$redisPort")
        throw new Exception(e)
    }
  }

}
