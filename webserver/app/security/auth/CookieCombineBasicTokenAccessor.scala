package security.auth

import commons.utils.Base64Utils
import jp.t2v.lab.play2.auth._
import play.api.mvc._

import scala.language.postfixOps

/**
 * Created by zhange on 2016-04-19.
 *
 */

class CookieCombineBasicTokenAccessor(
    protected val cookieName: String = "PSESS_ID",
    protected val cookieSecureOption: Boolean = false,
    protected val cookieHttpOnlyOption: Boolean = true,
    protected val cookieDomainOption: Option[String] = None,
    protected val cookiePathOption: String = "/",
    protected val cookieMaxAge: Option[Int] = None) extends TokenAccessor with Base64Utils {

  def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result = {
    val maxAge = 302400 // default 7 DAY
    val remember = request.tags.get("rememberme").exists("true" ==) || request.session.get("rememberme").exists("true" ==)
    val _maxAge = if (remember) Some(maxAge) else None
    val c = Cookie(cookieName, sign(token), _maxAge, cookiePathOption, cookieDomainOption, cookieSecureOption, cookieHttpOnlyOption)
    val header = "Authorization" -> s"Basic ${encodeBase64(token.toString)}"

    result.withCookies(c).withHeaders(header)
  }

  def extract(request: RequestHeader): Option[AuthenticityToken] = {
    val encoded = for {
      h <- request.headers.get("Authorization")
      if h.startsWith("Basic ") && h.length > 7 && !h.endsWith("_")
    } yield h.substring(6)
    val headerOpt = encoded.map(s => decodeBase64(s))
    headerOpt match {
      case h @ Some(_) => h
      case _           => request.cookies.get(cookieName).flatMap(c => verifyHmac(c.value))
    }
  }

  def delete(result: Result)(implicit request: RequestHeader): Result = {
    result.discardingCookies(DiscardingCookie(cookieName)).withHeaders("Authorization" -> s"Basic _")
  }
}