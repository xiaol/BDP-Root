package services.newsfeed

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Random

/**
 * Created by zhangshl on 17/3/16.
 */
object Test {

  def main(args: Array[String]) {
    //    val userdata: Future[String] = Future {
    //      Thread.sleep(2000)
    //      println("=====")
    //      "正确"
    //    }
    //    val aaa = try {
    //      Await.result(userdata, Duration(1000, TimeUnit.MILLISECONDS))
    //    } catch {
    //      case ex: Exception =>
    //        println(ex.getMessage)
    //        "错误"
    //    }
    //
    //    println(aaa)
    val style = Random.nextInt(2) match {
      case 1 => 1
      case _ => 11
    }
    println(style)
  }
}
