package utils

import com.typesafe.config.ConfigFactory
import redis.RedisClient

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait RedisDriver extends Config {
  implicit val systemRedisClient = akka.actor.ActorSystem("RedisClient", ConfigFactory.load("redis-akka"))
  val redisClient = RedisClient(redisHost, redisPort, redisPassword, Some(redisDB))
}

object RedisDriver extends RedisDriver {
  val cache = redisClient
}
