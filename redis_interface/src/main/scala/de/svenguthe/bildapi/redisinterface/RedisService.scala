package de.svenguthe.bildapi.redisinterface

import java.util.Calendar

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object RedisService {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val conf = ConfigFactory.load()

  private val redisHost = conf.getString("redis.host")
  private val redisPort = conf.getInt("redis.port")
  private val startpage = conf.getString("urls.politics.startpage")

  def getRedisServiceInstance(): RedisService = RedisService()

}

case class RedisService() {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def getRedisConnection() : RedisClient = {
    val redisService = new RedisService()
    val redisConnection = redisService.connectToRedis()

    redisConnection.get[String](RedisService.startpage.toString) match {
      case Some(exists) => logger.info(s"Redis allready initialized: ${RedisService.startpage.toString} - $exists")
      case _ =>
        logger.info("Initialize Redis")
        redisConnection.set(RedisService.startpage.toString, Calendar.getInstance.getTime)
    }

    redisConnection
  }

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
