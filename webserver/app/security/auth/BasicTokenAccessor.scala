package security.auth

import commons.utils.Base64Utils
import jp.t2v.lab.play2.auth.TokenAccessor
import jp.t2v.lab.play2.auth._
import play.api.mvc._

import scala.language.postfixOps

/**
 * Created by zhange on 2016-05-12.
 *
 */

class BasicTokenAccessor extends TokenAccessor with Base64Utils {

  private val tokenPrefix = "Basic "
  private val headerName = "Authorization"

  override def delete(result: Result)(implicit request: RequestHeader): Result = {
    result.withHeaders(headerName -> tokenPrefix)
  }

  override def put(token: AuthenticityToken)(result: Result)(implicit request: RequestHeader): Result = {
    result.withHeaders(headerName -> s"$tokenPrefix${encodeBase64(token.toString)}")
  }

  override def extract(request: RequestHeader): Option[AuthenticityToken] = {
    val encoded = for {
      h <- request.headers.get(headerName)
      if h.startsWith(tokenPrefix) && h.length > 6
    } yield h.substring(tokenPrefix.length)
    encoded.map(s => decodeBase64(s))
  }
}
