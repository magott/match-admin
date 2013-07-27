package service

import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.TimeUnit
import data.{User, Session}

trait SessionRepository {
  def sessionById(sessionId:String) : Option[Session]
  def userForSession(sessionId:String) : Option[User]
  def saveUser(user:User) : Option[User]
}

trait CachingSessionRepository extends SessionRepository{

  abstract override def sessionById(sessionId:String) : Option[Session] = {
    import CachingSessionRepository.sessionCache._
    Option(getIfPresent(sessionId)).orElse{
      val sessionOption = super.sessionById(sessionId)
      sessionOption.foreach(session=> put(sessionId,session))
      sessionOption
    }
  }

  abstract override def userForSession(sessionId:String) : Option[User]= {
    val sessionOption = sessionById(sessionId)
    import CachingSessionRepository.userCache._
    sessionOption.flatMap(session=> Option(getIfPresent(session.username)).orElse{
      val userOpt = sessionOption.flatMap(session => super.userForSession(session.sessionId))
      userOpt.foreach(user=> put(user.email,user))
      userOpt
    })
  }

  abstract override def saveUser(user:User) : Option[User] = {
    import CachingSessionRepository._
    userCache.invalidate(user.email)
    super.saveUser(user)
  }


}

object CachingSessionRepository{
  private val sessionCache:Cache[String,Session] = CacheBuilder.newBuilder.maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build()
  private val userCache:Cache[String,User] = CacheBuilder.newBuilder.maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build()
}
