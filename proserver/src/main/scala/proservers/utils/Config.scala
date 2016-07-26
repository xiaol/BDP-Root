package proservers.utils

import com.typesafe.config.ConfigFactory

/**
 * Created by zhange on 2016-05-19.
 *
 */

trait Config {

  val config = ConfigFactory.load()

  val redisConfig = config.getConfig("redis")
  val redisHost = redisConfig.getString("host")
  val redisPort = redisConfig.getInt("port")
  val tryPassword = redisConfig.getString("password")
  val redisPassword: Option[String] = if (tryPassword.nonEmpty) Some(tryPassword) else None

  val serviceConfig = config.getConfig("RPC-Service")
  val asearchURI = serviceConfig.getString("URI-ASearch")
  val asearchDBURI = serviceConfig.getString("DB-ASearch")
  val newsDBURI = serviceConfig.getString("DB-News")

  val imageTempPath = config.getString("image-temp-path")

  val ossConfig = config.getConfig("oss")
  val endpoint = ossConfig.getString("endpoint")
  val accessKeyId = ossConfig.getString("key")
  val accessKeySecret = ossConfig.getString("secret")
  val ossPrefixUri = ossConfig.getString("prefix-uri")
  val ossBucketName = ossConfig.getString("bucket")
}
