package commons.models.userprofiles

import org.joda.time.LocalDateTime

/**
 * Created by zhange on 2016-07-13.
 *
 */

case class ConcernPublisherRow(
  id: Option[Long] = None,
  ctime: LocalDateTime,
  uid: Long,
  pname: String)
