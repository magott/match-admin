package web

import unfiltered.request.{Cookies, HttpRequest}
import service.MongoRepository._

object AdminSession {
  def unapply(req: HttpRequest[_]) = {
    SessionId.unapply(req) match{
      case Some(sessionId)  => sessionById(sessionId) match {
        case Some(session) if(session.admin) => Some(session)
        case _ => None
      }
      case _ => None
    }
  }
}

object UserSession {
  def unapply(req: HttpRequest[_]) = {
    SessionId.unapply(req) match{
      case Some(sessionId) => sessionById(sessionId)
      case None => None
    }
  }
}

object LoggedOnUser {
  def unapply(req: HttpRequest[_]) = {
    SessionId.unapply(req) match{
      case Some(sessionId) => userForSession(sessionId)
      case None => None
    }
  }
}

object SessionId{
  def unapply[T](req: HttpRequest[T]) = {
    val cookies = Cookies.unapply(req).get
    cookies("user.sessionId") match {
      case Some(c) => Some(c.value)
      case None => None
    }
  }
}
