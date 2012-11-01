package web

import unfiltered.request._

import scala.Predef._
import service.MongoRepository
import org.mindrot.jbcrypt.BCrypt
import data.Session
import unfiltered.response._
import web.HerokuRedirect.XForwardProto
import unfiltered.response.Html5
import unfiltered.Cookie
import scala.Some
import unfiltered.response.ResponseString

class LoginHandler {

  def handleLogin(req: HttpRequest[_]) = {
    req match{
      case Path("/login") => req match{
        case XForwardProto("http") => HerokuRedirect(req,"login")
        case GET(_) & LoggedOnUser(user)=> Html5(Pages(req).alreadyLoggedIn)
        case GET(_) & Params(qp)=> Html5(Pages(req).login)
        case POST(_) & Params(p) => {
          processLogin(req,p)
        }
      }
    }
  }

  def handleLogout(req:HttpRequest[_]) = {
    req match {
      case GET(_) => processLogout(req)
      case _ => MethodNotAllowed ~> ResponseString("Dette skal du ikke gjøre")
    }
  }

  def handlePasswordReset(req:HttpRequest[_]) = {
    Ok ~> ResponseString("To be implemented")
  }

  def processLogin(req:HttpRequest[_], p:Map[String, Seq[String]]) = {
    def userCookie(value:String, rememberMe:Boolean) = {
      val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
      val cookie = Cookie(name = "user.sessionId", value = value, secure = secure, path = Some("/"))
      if(rememberMe) cookie.copy(maxAge=Some(3600*24*365))else cookie
    }
    val email = p.get("email")
    val password = p.get("password")
    val rememberMe = p.get("remember").exists(_.contains("on"))
    if(email.isEmpty || password.isEmpty){
      HerokuRedirect(req, "/login?failed")
    }else{
      val userOpt = MongoRepository.userByEmail(email.get.head)
      if(userOpt.exists(user => BCrypt.checkpw(password.get.head,user.password))){
        val session = Session.newInstance(userOpt.get)
        MongoRepository.newSession(session)
        SetCookies(userCookie(session.sessionId, rememberMe)) ~> HerokuRedirect(req, "/matches")
      }else{
        HerokuRedirect(req,"/login?failed")
      }
    }
  }

  def processLogout(req:HttpRequest[_]) = {
    val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
    SetCookies(Cookie(name="user.sessionId",value="", secure=secure, path=Some("/"), maxAge = Some(0))) ~>
    HerokuRedirect(req, "/matches")
  }

}
