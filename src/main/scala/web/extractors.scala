package web

import unfiltered.request.{Cookies, HttpRequest}
import service.MongoRepository._
import data.{User, Session}
import service.{CachingSessionRepository, MongoRepository}

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
  val repo = MongoRepository.singletonWithSessionCaching
  def unapply(req: HttpRequest[_]) = {
    SessionId.unapply(req) match{
      case Some(sessionId)  => repo.sessionById(sessionId) match {
        case Some(session) if(session.admin) => Some(session)
        case _ => None
      }
      case _ => None
    }
  }
}

object UserSession {
  val repo = MongoRepository.singletonWithSessionCaching
  def unapply(req: HttpRequest[_]) : Option[Session] = {
    SessionId.unapply(req).flatMap(sessionId => repo.sessionById(sessionId))
  }
}

object LoggedOnUser {
  val repo = MongoRepository.singletonWithSessionCaching
  def unapply(req: HttpRequest[_]) : Option[User] = {
    SessionId.unapply(req).flatMap(sessionId => repo.userForSession(sessionId))
  }
}

object SessionId{
  def unapply[T](req: HttpRequest[T]):Option[String] = {
   Cookies.unapply(req).flatMap(cookies => cookies("user.sessionId").map(_.value))
  }
}
