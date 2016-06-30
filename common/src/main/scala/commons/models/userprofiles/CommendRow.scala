package commons.models.userprofiles

import org.joda.time.LocalDateTime

/**
 * Created by zhange on 2016-05-11.
 *
 */

case class CommendRow(
  id: Option[Long] = None,
  ctime: LocalDateTime,
  uid: Long,
  cid: Long)
