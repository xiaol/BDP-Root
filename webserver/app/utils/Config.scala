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
  val redisDB = redisConfig.getInt("database")
  val tryPassword = redisConfig.getString("password")
  val redisPassword: Option[String] = if (tryPassword.nonEmpty) Some(tryPassword) else None

  val clusterConfig = config.getConfig("cluster")
  val dispatcherPathConfig = clusterConfig.getString("DispatcherPath")

  val elasticConfig = config.getConfig("elasticsearch")
  val elasticClusterUrl = elasticConfig.getString("cluster")

  val ad = config.getConfig("ad")
  val appids = ad.getString("appid")
  val appkeys = ad.getString("appkey")
  val urls = ad.getString("url")

  val deleteAd = config.getConfig("deleteAd")
  val deleteAdHost = deleteAd.getString("host")
  val deleteAdPort = deleteAd.getString("port")
  val deleteAdPath = deleteAd.getString("path")

  val email = config.getConfig("email")
  val emailhostname = email.getString("emailhostname")
  val emailpost = email.getInt("emailpost")
  val emailfrom = email.getString("emailfrom")
  val emailusername = email.getString("emailusername")
  val emailpassword = email.getString("emailpassword")
  val emailsubject = email.getString("emailsubject")
  val emailwelcome = email.getString("emailwelcome")
}
