package web {

import unfiltered.request._
import unfiltered.response._
import unfiltered.response.Redirect

object HerokuRedirect {
  def apply[A, B](req: HttpRequest[A], path: String): ResponseFunction[B] = {
    val absolutepath = if (path.startsWith("/")) path else "/" + path
    req match {
      case XForwardProto(_) & Host(host) => Found ~> Location("https://%s%s".format(host, absolutepath))
      case _ => Redirect(absolutepath)
    }
  }


}

object XForwardProto extends StringHeader("x-forwarded-proto")



}

