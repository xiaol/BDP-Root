package utils

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait AdConfig extends Config {

}

object AdConfig extends AdConfig {
  val adappid = appids
  val adappkey = appkeys
  val adurl = urls

  val deleteAdHost_v = deleteAdHost
  val deleteAdPort_v = deleteAdPort
  val deleteAdPath_v = deleteAdPath

  val adWeight = config.getConfig("adsource").getConfig("weight")
  val lieyingapiWeight = adWeight.getString("lieyingapi").toInt
  val gdtsdkWeight = adWeight.getString("gdtsdk").toInt
  val yifuapiWeight = adWeight.getString("yifuapi").toInt
}
