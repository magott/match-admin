package web

import unfiltered.request.{Cookies, HttpRequest}
import service.MongoRepository._
import data.{User, Session}

object NotAdmin{
  def unapply(req:HttpRequest[_]) = {
    LoggedOnUser.unapply(req) match{
      case None => Some("authentication needed")
      case Some(u) if(!u.admin) => Some("access denied")
      case Some(u) => None
    }
  }
}

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
  def unapply(req: HttpRequest[_]) : Option[Session] = {
    SessionId.unapply(req).flatMap(sessionId => sessionById(sessionId))
  }
}

object LoggedOnUser {
  def unapply(req: HttpRequest[_]) : Option[User] = {
    SessionId.unapply(req).flatMap(sessionId => userForSession(sessionId))
  }
}

object SessionId{
  def unapply[T](req: HttpRequest[T]):Option[String] = {
   Cookies.unapply(req).flatMap(cookies => cookies("user.sessionId").map(_.value))
  }
}
