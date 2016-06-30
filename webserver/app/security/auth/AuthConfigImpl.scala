package security.auth

import java.security.SecureRandom

import commons.models.users.UserRole._
import commons.models.users._
import jp.t2v.lab.play2.auth._
import play.api.mvc.Results._
import play.api.mvc._

import scala.reflect.{ ClassTag, classTag }
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.annotation.tailrec
import scala.util.Random
import controllers.routes
import services.users.UserCacheService
import utils.Response._
import utils.RedisDriver.cache

/**
 * Created by zhange on 2016-04-19.
 *
 */

trait AuthConfigImpl extends AuthConfig with UserCacheService {

  import services.users.IUserService

  type Id = Long
  type User = UserRow
  type Authority = UserRole
  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600 * 12 * 7
  val userService: IUserService

  def resolveUser(uid: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = {
    getUserRowCache(uid).flatMap {
      case userRow @ Some(_) => Future.successful(userRow)
      case _ => userService.findByUid(uid).map {
        case Some(u) =>
          setUserRowCache(u); Some(u)
        case _ => None
      }
    }
  }

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.UserController.afterLogin()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.UserController.afterLogout()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(AuthVerifyError("LoginFirst"))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(AuthVerifyError("NoPermission"))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    (user.sys.urole, authority) match {
      case (ADMIN_ROLE_CODE, _)           => true
      case (REGIST_ROLE_CODE, RegistRole) => true
      case (REGIST_ROLE_CODE, GuestRole)  => true
      case (GUEST_ROLE_CODE, GuestRole)   => true
      case _                              => false
    }
  }

  override lazy val tokenAccessor = new BasicTokenAccessor

  override lazy val idContainer: AsyncIdContainer[Id] = new AsyncIdContainer[Id] {

    private val tokenSuffix = ":token"
    private val userIdSuffix = ":userId"
    private val random = new Random(new SecureRandom())

    override def startNewSession(userId: Id, timeoutInSeconds: Int)(implicit request: RequestHeader, context: ExecutionContext): Future[AuthenticityToken] = {
      removeByUserId(userId)
      val token = generate()
      store(token, userId, timeoutInSeconds)
      Future.successful(token)
    }

    @tailrec
    private final def generate(): AuthenticityToken = {
      val table = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
      val token = Iterator.continually(random.nextInt(table.length)).map(table).take(64).mkString
      if (syncGet(token).isDefined) generate() else token // 递归检查缓存中是否已存在该token,存在则再次生成
    }

    private def removeByUserId(userId: Id) = {
      cache.get[String](userId.toString + userIdSuffix).map {
        case Some(t) => unsetToken(t.asInstanceOf[AuthenticityToken])
        case _       =>
      }
      unsetUserId(userId)
    }

    override def remove(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Unit] = {
      get(token).map {
        case Some(id) => unsetUserId(id)
        case None     =>
      }
      Future.successful(unsetToken(token))
    }

    private def unsetToken(token: AuthenticityToken) = {
      cache.del(token + tokenSuffix)
    }
    private def unsetUserId(userId: Id) = {
      cache.del(userId.toString + userIdSuffix)
    }

    override def get(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Option[Id]] = {
      for {
        uidOpt <- cache.get[String](token + tokenSuffix)
      } yield {
        uidOpt.map(_.toLong)
      }
    }

    private def syncGet(token: AuthenticityToken): Option[Id] = {
      try {
        Await.result(cache.get[String](token + tokenSuffix), 2.seconds).map(_.toLong)
      } catch {
        // If timeout, return a fake UID avoid duplicate token.
        case e: TimeoutException => Some(1L)
      }
    }

    private def store(token: AuthenticityToken, userId: Id, timeoutInSeconds: Int) = {
      cache.set(token + tokenSuffix, userId.toString, Some(timeoutInSeconds.toLong))
      cache.set(userId.toString + userIdSuffix, token.toString, Some(timeoutInSeconds.toLong))
    }

    override def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int)(implicit request: RequestHeader, context: ExecutionContext): Future[Unit] = {
      get(token).map {
        case Some(id) => store(token, id, timeoutInSeconds)
        case _        =>
      }
    }
  }

}
