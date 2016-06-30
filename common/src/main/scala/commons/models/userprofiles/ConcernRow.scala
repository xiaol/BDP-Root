package commons.models.userprofiles

import org.joda.time.LocalDateTime

/**
 * Created by zhange on 2016-05-11.
 *
 */

case class ConcernRow(
  id: Option[Long] = None,
  ctime: LocalDateTime,
  uid: Long,
  nid: Long)
