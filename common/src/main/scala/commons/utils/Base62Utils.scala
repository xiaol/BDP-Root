package commons.utils

/**
 * Created by zhange on 16/4/7.
 *
 */

trait Base62Utils {

  val baseString = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val base = 62

  if (baseString.length != base) {
    throw new IllegalArgumentException("baseString length must be %d".format(base))
  }

  def decode(s: String): Long = {
    s.zip(s.indices.reverse)
      .map { case (c, p) => baseString.indexOf(c) * scala.math.pow(base, p).toLong }
      .sum
  }

  def encode(i: Long): String = {

    @scala.annotation.tailrec
    def div(i: Long, res: List[Int] = Nil): List[Int] = {
      i / base match {
        case q if q > 0 => div(q, (i % base).toInt :: res)
        case _          => i.toInt :: res
      }
    }

    div(i).map(x => baseString(x)).mkString
  }

  def weiboMid2Id(mid: String) = {
    var id = ""
    for (x <- Range(mid.length, 0, -4)) {
      val startIndex = if (x - 4 < 0) 0 else x - 4
      var temp = decode(mid.substring(startIndex, x)).toString
      if (startIndex > 0 && temp.length < 7) {
        temp = temp.reverse.padTo(7, '0').reverse
      }
      id = temp + id
    }
    id
  }

  def weiboIdToMid(id: String) = {
    var mid = ""
    for (x <- Range(id.length, 0, -7)) {
      val startIndex = if (x - 7 < 0) 0 else x - 7
      val temp = encode(id.substring(startIndex, x).toLong).toString
      mid = temp + mid
    }
    mid
  }
}

