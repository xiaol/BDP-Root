package utils

import com.typesafe.config.ConfigFactory

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait Config {
  val config = ConfigFactory.load()

  val redisConfig = config.getConfig("redis")
  val redisHost = redisConfig.getString("host")
  val redisPort = redisConfig.getInt("port")
  val tryPassword = redisConfig.getString("password")
  val redisPassword: Option[String] = if (tryPassword.nonEmpty) Some(tryPassword) else None

  val clusterConfig = config.getConfig("cluster")
  val dispatcherPathConfig = clusterConfig.getString("DispatcherPath")

  val elasticConfig = config.getConfig("elasticsearch")
  val elasticClusterUrl = elasticConfig.getString("cluster")
}
