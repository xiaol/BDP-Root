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

  val adSource = config.getConfig("adsource")
  val adSourceIos = adSource.getConfig("ios")
  val adSourceAndroid = adSource.getConfig("android")

  val adIosWeight = adSourceIos.getConfig("weight")
  val adIosPos = adSourceIos.getConfig("displayPosition")
  val adAndroidWeight = adSourceAndroid.getConfig("weight")
  val adAndroidPos = adSourceAndroid.getConfig("displayPosition")

  val adQDZX_I = adSourceIos.getConfig("qidianzixun")
  val adHLTQ_I = adSourceIos.getConfig("huanglitianqi")
  val adLYLLQ_I = adSourceIos.getConfig("lieyingliulanqi")

  val adQDZX_A = adSourceAndroid.getConfig("qidianzixun")
  val adLYLLQ_A = adSourceAndroid.getConfig("lieyingliulanqi")
  val adWZSP_A = adSourceAndroid.getConfig("weizisuoping")
  val adBPYZ_A = adSourceAndroid.getConfig("baipai4yuzhuang")
  val adBPYYH_A = adSourceAndroid.getConfig("baipai4yingyonghui")

}
