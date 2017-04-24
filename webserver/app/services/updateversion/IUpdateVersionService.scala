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
  def compare(uid: Long, channelId: Int, ptype: Int, version: String, version_code: Int): Future[Option[UpdateVersion]]
}

class UpdateVersionService @Inject() (val updateVersionDAO: UpdateVersionDAO) extends IUpdateVersionService with OtherCacheService {

  def compare(uid: Long, channelId: Int, ptype: Int, version: String, version_code: Int): Future[Option[UpdateVersion]] = {
    {
      //从缓存取数据
      getUpdateVersionCache(channelId: Int, ptype: Int).flatMap { value =>
        value match {
          //存在，满足条件，直接返回数据
          case Some(value) if value.version_code > version_code && !value.version.equals(version) => Future { Some(value) }
          //不存在从数据库取， 再存入缓存
          case None => updateVersionDAO.findUpdateVersion(channelId: Int, ptype: Int).map { value =>
            value match {
              case Some(version_n) =>
                setUpdateVersionCache(version_n)
                if (version_n.version_code > version_code && !version_n.version.equals(version))
                  Some(version_n)
                else
                  Some(version_n.copy(updateLog = Some("nonUpdate")))
              case _ => None
            }
          }
          //上面的None未执行，一定有值，但版本一样
          case _ => Future { Some(value.get.copy(updateLog = Some("nonUpdate"))) }
        }
      }
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UpdateVersionService.compare($channelId, $ptype, $version_code): ${e.getMessage}")
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
      val newvalue = updateVersion.copy(version_code = updateVersion.version_code + 1)
      setUpdateVersionCache(newvalue)
      updateVersionDAO.update(newvalue)
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Within UpdateVersionService.update($updateVersion): ${e.getMessage}")
        None
    }
  }

}