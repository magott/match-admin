package web

import javax.servlet.http.HttpServletRequest
import unfiltered.request.{Cookies, HttpRequest}
import service.MongoRepository

object LoggedOnUser {

  def unapply[T <: HttpServletRequest](req: HttpRequest[T]) = {
    UserId.unapply(req) match{
      case Some(sessionId) => MongoRepository.userForSession(sessionId)
      case None => None

    }
  }
}

object UserId{
  def unapply[T](req: HttpRequest[T]) = {
    val cookies = Cookies.unapply(req).get
    cookies("user.sessionId") match {
      case Some(c) => Some(c.value)
      case None => None
    }
  }
}
