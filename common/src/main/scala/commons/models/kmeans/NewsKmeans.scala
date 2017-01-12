package commons.models.kmeans

import org.joda.time.LocalDateTime

/**
 * Created by zhangshl on 17/1/11.
 */
case class NewsKmeans(nid: Long,
                      model_v: String,
                      ch_name: String,
                      cluster_id: Long,
                      chid: Long)

case class UserKmeansCluster(uid: Long,
                             model_v: String,
                             ch_name: String,
                             cluster_id: Long,
                             chid: Long,
                             times: Int,
                             create_time: Option[LocalDateTime] = None,
                             fail_time: Option[LocalDateTime] = None)
