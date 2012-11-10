package web

import unfiltered.request._
import unfiltered.response._
import data.{Session, User}
import data.MatchValidation.UserValidation
import scala.Left
import unfiltered.Cookie
import scala.Right
import scala.Some
import unfiltered.response.ResponseString
import service.MongoRepository
import org.bson.types.ObjectId
import org.joda.time.DateTime

class UserHandler {

  def handleUser(req: HttpRequest[_])  = {
    req match {
      case Path("/users/signup") => {
        req match{
          case GET(_) => Html5(Pages(req).userForm(None))
          case POST(_) => {
            val Params(p) = req
            handleSignup(p) match{
              case Right(sessionId) => {
                SetCookies(userCookie(req,sessionId)) ~> HerokuRedirect(req,"/matches")
              }
              case Left(errors) => BadRequest ~> Html5(Pages(req).errorPage(errors.map( e=> <p>{e}</p>)))
            }
          }
        }
      }
      case Path(Seg("users" :: userId :: Nil)) => {
        req match{
          case GET(_) =>{
            LoggedOnUser.unapply(req) match{
              case Some(user) if(user.id.get == userId) => Html5(Pages(req).userForm(Some(user)))
              case Some(user) => Forbidden ~> Html5(Pages(req).forbidden)
              case None => Forbidden ~> Html5(Pages(req).forbidden) //Hva er rett kode??
            }
          }
          case POST(_) =>{
            val Params(p) = req
            LoggedOnUser.unapply(req) match{
              case Some(user) if(user.id.get == userId) => handleEditUser(p, userId, SessionId.unapply(req).get) match{
                case Right(_) => HerokuRedirect(req, "/matches")
                case Left(errors) => BadRequest ~> Html5(Pages(req).errorPage(errors.map(e => <p>{e}</p>)))
              }
              case Some(user) => Forbidden ~> ResponseString("Fuck")
              case None => Forbidden ~> Html5(Pages(req).forbidden)
            }
          }
          case _ => MethodNotAllowed
        }
      }
      case r@_ => NotFound ~> Html5(Pages(r).notFound())
    }
  }

  def handleSignup(params: Map[String, Seq[String]]) : Either[List[String], String] = {
    userFromParams(params, None).right.map{ u:User =>
        val user = MongoRepository.saveUser(u)
        val session = Session.newInstance(user.get)
        MongoRepository.newSession(session)
        session.sessionId
      }
    }

  def handleEditUser(params: Map[String, Seq[String]], userId:String, sessionId:String) : Either[List[String], String] = {
    userFromParams(params, Some(userId)).right.map{u:User=>
      val updatedUser = u.copy(admin = MongoRepository.userById(new ObjectId(userId)).get.admin)
      MongoRepository.saveUser(updatedUser)
      val currentSession = MongoRepository.sessionById(sessionId).get
      MongoRepository.updateUserSessions(currentSession.username, updatedUser)
      sessionId
    }
  }


  def userFromParams(params: Map[String, Seq[String]], userId:Option[String]): Either[List[String], User] = {
    def valueOrBlank(key:String) :String = params.getOrElse(key,List("")).head
    val validation = UserValidation.validate(userId.map(i=>new ObjectId(i)),  valueOrBlank("name"), valueOrBlank("email"), valueOrBlank("telephone"), valueOrBlank("level"), valueOrBlank("refNumber"),valueOrBlank("password"), valueOrBlank("password2"))
    validation
  }



  def userCookie(req:HttpRequest[_], value:String) = {
    val secure = req match { case XForwardProto("https") => Some(true) case _ => Some(false)}
    Cookie(name="user.sessionId",value=value, secure=secure, path=Some("/"))
  }

}
