package service

import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.TimeUnit
import data.{User, Session}

trait SessionRepository {
  def sessionById(sessionId:String) : Option[Session]
  def userForSession(sessionId:String) : Option[User]
}

trait CachingSessionRepository extends SessionRepository{

  abstract override def sessionById(sessionId:String) : Option[Session] = {
    Option(CachingSessionRepository.sessionCache.getIfPresent(sessionId)).orElse{
      val sessionOption = super.sessionById(sessionId)
      sessionOption.foreach(session=> CachingSessionRepository.sessionCache.put(sessionId,session))
      sessionOption
    }
  }

  abstract override def userForSession(sessionId:String) : Option[User]= {
    Option(CachingSessionRepository.userCache.getIfPresent(sessionId)).orElse{
      val sessionOption = super.userForSession(sessionId)
      sessionOption.foreach(session=> CachingSessionRepository.userCache.put(sessionId,session))
      sessionOption
    }
  }
}

object CachingSessionRepository{
  private val sessionCache:Cache[String,Session] = CacheBuilder.newBuilder.maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build()
  private val userCache:Cache[String,User] = CacheBuilder.newBuilder.maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES).build()
}
