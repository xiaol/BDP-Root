package utils

import org.elasticsearch.common.settings.Settings
import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri }

/**
 * Created by zhangshl on 2016-046-17.
 *
 */

trait EsDriver extends Config {
  val settings: Settings = Settings.settingsBuilder().put("cluster.name", "es-cluster").build()
  val client = ElasticClient.transport(settings, ElasticsearchClientUri(elasticClusterUrl))
}

object EsDriver extends EsDriver {
  val esClient = client
}