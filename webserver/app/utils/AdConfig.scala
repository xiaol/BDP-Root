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
}
