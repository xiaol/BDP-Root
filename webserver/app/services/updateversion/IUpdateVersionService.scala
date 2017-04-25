package services.updateversion

import javax.inject.Inject

import com.google.inject.ImplementedBy
import commons.models.updateversion.UpdateVersion
import dao.updateversion.UpdateVersionDAO
import play.api.Logger
import services.OtherCache.OtherCacheService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * Created by zhangshl on 16/7/15.
 */
@ImplementedBy(classOf[UpdateVersionService])
trait IUpdateVersionService {
  def query(ctype: Int, ptype: Int): Future[Option[UpdateVersion]]
}

class UpdateVersionService @Inject() (val updateVersionDAO: UpdateVersionDAO) extends IUpdateVersionService with OtherCacheService {

  def query(ctype: Int, ptype: Int): Future[Option[UpdateVersion]] = {
    {
      //从缓存取数据
      getUpdateVersionCache(ctype: Int, ptype: Int).flatMap { value =>
        value match {
          //存在，满足条件，直接返回数据
          case Some(value) => Future { Some(value) }
          //不存在从数据库取， 再存入缓存
          case None => updateVersionDAO.findUpdateVersion(ctype: Int, ptype: Int).map { value =>
            value match {
              case Some(version_n) =>
                setUpdateVersionCache(version_n)
                Some(version_n)
              case _ => None
            }
          }
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UpdateVersionService.compare($ctype, $ptype): ${e.getMessage}")
        None
    }
  }

  def insert(updateVersion: UpdateVersion): Future[Int] = {
    {
      setUpdateVersionCache(updateVersion)
      updateVersionDAO.insert(updateVersion)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UpdateVersionService.insert($updateVersion): ${e.getMessage}")
        0
    }
  }

  def update(updateVersion: UpdateVersion): Future[Option[UpdateVersion]] = {
    {
      setUpdateVersionCache(updateVersion)
      updateVersionDAO.update(updateVersion)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UpdateVersionService.update($updateVersion): ${e.getMessage}")
        None
    }
  }

}