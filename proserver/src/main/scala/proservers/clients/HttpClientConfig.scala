package proservers.clients

import com.typesafe.config.Config
import scala.util.Try

/**
 * Created by zhange on 2016-06-14.
 *
 */

case class HttpClientConfig(host: String, port: Int, tls: Boolean)

object HttpClientConfig {
  def apply(config: Config): HttpClientConfig =
    HttpClientConfig(
      config.getString("host"),
      Try(config.getInt("port")).getOrElse(80),
      Try(config.getBoolean("tls")).toOption.getOrElse(false)
    )
}
