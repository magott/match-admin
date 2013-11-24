package web

import unfiltered.request._

import scala.Predef._
import service.MongoRepository._
import data.Session
import unfiltered.response._
import org.mindrot.jbcrypt.BCrypt._
import unfiltered.response.Html5
import unfiltered.{response, Cookie}
import scala.Some
import unfiltered.response.ResponseString
import service.{MongoRepository, MailgunService, ResetPasswordService}
import org.joda.time.DateTime

class LoginHandler(private val repo:MongoRepository) {

  val passwordService = new ResetPasswordService

  def handleLogin(req: HttpRequest[_]) = {
    req match{
      case Path("/login") => req match{
        case XForwardProto("http") => HerokuRedirect(req,"login")
        case GET(_) & LoggedOnUser(user)=> Html5(Pages(req).alreadyLoggedIn)
        case GET(_) & Params(qp)=> Ok ~> SetCookies(Cookie(name="COOKIE_CHECK",value="check", maxAge = Some(2000))) ~> Html5(Pages(req).login(qp))
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
    req match {
      case XForwardProto("http") => HerokuRedirect(req, "/restpassword")
      case GET(_) & Params(IdParam(id)) => {
        if(passwordService.validResetId(id))
          Ok ~> Html5(Pages(req).resetPassword)
        else
          NotFound ~> Html5(Pages(req).errorPage(<p>Forespørselen er ugyldig eller for gammel. Resetting av passord må gjøres innen 30 minutter fra du mottok e-post med instruksjoner <a href="/lostpassword">Forsøk å resette passordet på nytt</a></p>))
      }
      case GET(_) => BadRequest ~> Html5(Pages(req).errorPage(<p>Siden finnes ikke</p>))
      case POST(_) & Params(IdParam(id)) & Params(PasswordParam(pwd)) => {
        passwordService.emailForResetId(id) match{
          case None => BadRequest ~> Html5(Pages(req).errorPage(<p>Kan ikke sette nytt passord, forespørselen er ugyldig eller for gammel. <a href="/lostpassword">Forsøk på nytt</a></p>))
          case Some(email) => repo.userByEmail(email) match{
            case None => BadRequest ~> Html5(Pages(req).errorPage(<p>Kan ikke sette nytt passord, forespørselen er ugyldig eller for gammel. <a href="/lostpassword">Forsøk på nytt</a></p>))
            case Some(user) => {
              repo.saveUser(user.copy(password = hashpw(pwd, gensalt())))
              HerokuRedirect(req, "/login?reset")
            }
          }
        }
      }
      case _ => BadRequest ~> Html5(Pages(req).errorPage(<p>Ugyldig forespørsel</p>))
    }
  }

  def handleLostPassword(req:HttpRequest[_]) = {
    req match{
      case GET(_) => Ok ~> Html5(Pages(req).lostPassword)
      case POST(_) & Params(EmailParam(email)) =>{
        val resetId = passwordService.generateResetId(email)
        val Host(host) = req
        val protocol = XForwardProto.unapply(req).getOrElse("http")
        val resetUrl = "%s://%s/resetpassword?id=%s".format(protocol,host,resetId)
        println(resetUrl)
        if(repo.userByEmail(email).isDefined)
          MailgunService.sendLostpasswordMail(email, resetUrl)
        HerokuRedirect(req, "/login?checkmail")
      }

    }
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
      val userOpt = repo.userByEmail(email.get.head.toLowerCase)
      if(userOpt.exists(user => checkpw(password.get.head,user.password))){
        val session = if(rememberMe) Session.newInstance(userOpt.get, DateTime.now.plusYears(1)) else Session.newInstance(userOpt.get)
        repo.newSession(session)
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

  object IdParam extends Params.Extract("id", Params.first)
  object PasswordParam extends Params.Extract("password", Params.first)
  object EmailParam extends Params.Extract("email", Params.first)

}
