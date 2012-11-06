package service

import com.google.common.cache.{Cache, CacheBuilder}
import java.util.concurrent.TimeUnit
import java.util.UUID
import scala.Predef._

class ResetPasswordService {

  private val resetPasswordStorage:Cache[String,String] = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(100).build()

  def validResetId(id:String) = Option(resetPasswordStorage.getIfPresent(id)).isDefined

  def generateResetId(email:String) = {
    val id = UUID.randomUUID().toString
    resetPasswordStorage.put(id,email)
    id
  }

  def emailForResetId(id:String) = {
    val email = Option(resetPasswordStorage.getIfPresent(id))
    resetPasswordStorage.invalidate(id)
    email
  }

}
