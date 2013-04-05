package web

import unfiltered.request._
import unfiltered.response._
import service.MongoRepository._
import org.bson.types.ObjectId
import unfiltered.response.Html5
import unfiltered.response.ResponseString
import data._
import java.util.Date
import org.joda.time.{LocalDate, DateMidnight, DateTime}
import service.{MongoRepository, MailgunService}
import scala.Left
import data.MailAccepted
import scala.Right
import scala.Some
import unfiltered.response.Html5
import unfiltered.response.ResponseString

class AdminHandler(private val repo:MongoRepository) {
  import repo._

  def handleAdmin(req: HttpRequest[_]) = {
    req match {
      case Path(Seg("admin" :: "matches" :: "new" :: Nil)) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => Html5(Pages(req).editMatchForm(None))
        case _ => MethodNotAllowed
      }
      case Path(Seg(List("admin", "matches"))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>{
          if(viewAll(req))
            Html5(Pages(req).listMatchesWithFilters(listPublishedMatchesNewerThan(new DateMidnight(2000,1,1).toDateTime), "/admin/matches/", None))
          else
            Html5(Pages(req).listMatchesWithFilters(listPublishedMatchesNewerThan(DateTime.now.withDayOfYear(1)), "/admin/matches/", None))
        }
        case POST(_) & Params(p)=>{
          matchFromParams(None, p) match{
            case Left(errors) => Html5(Pages(req).errorPage(errors.map(e => <p>{e}</p>)))
            case Right(m) => {
              saveMatch(m)
              HerokuRedirect(req, "/admin/matches")
            }
          }
        }
      }
      case Path(Seg(List("admin","matches", matchId))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>{
          fullMatch(new ObjectId(matchId)) match {
            case None => BadRequest ~> Html5(Pages(req).notFound(Some("Ingen kamp med id " + matchId)))
            case Some(m) => Html5(Pages(req).editMatchForm(Some(m)))
          }
        }
        case POST(_) & Params(p)=>{
          matchFromParams(Some(matchId), p) match{
            case Left(errors) => Html5(Pages(req).errorPage(errors.map(e => <p>{e}</p>)))
            case Right(m) => {
              saveMatch(m)
              HerokuRedirect(req, "/admin/matches")
            }
          }
        }
        case DELETE(_) => {
          deleteMatch(new ObjectId(matchId))
          Ok ~> JsonContent ~> ResponseString("""{"href": "/admin/matches"}""")
        }
      }
      case Path(Seg("admin" :: "matches" :: matchId :: "sendmail" :: Nil)) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case _ => {
          fullMatch(new ObjectId(matchId)) match{
            case Some(m) => MailgunService.sendAppointmentMail(m) match{
              case MailAccepted(message) => Ok
              case MailRejected(code, message) =>{
                println("Mailsending error code %s message %s".format(message,code))
                InternalServerError
              }
            }
            case None => NotFound ~> JsonContent ~> ResponseString("""{"error":"Fant ikke kampen"}""")
          }
        }
      }
      case Path(Seg("admin" :: "users" :: Nil)) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => Ok ~> Html5(Pages(req).userList(allUsers))
      }
      case Path(Seg(List("admin", "users", userId))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>
          userById(new ObjectId(userId)) match{
            case Some(user) => Ok ~> Html5(Pages(req).user(user, repo.matchesWithReferee(user.id.get), "/admin/matches/"))
            case None => NotFound ~> Html5(Pages(req).notFound(Some("Fant ingen dommer med id %s".format(userId))))
          }
        case _ => MethodNotAllowed
      }
      case Path(Seg("admin" :: "matches" :: "orders" :: Nil)) => req match{
        case GET => Html5(<html>Here be unpublished matches</html>)
      }
      case _ => NotFound ~> Html5(Pages(req).notFound(Some("Ukjent adminside")))
    }
  }

  def matchFromParams(id:Option[String], p:Map[String, Seq[String]]) = {
    val params = p.withDefaultValue(List(""))
    MatchValidation.validate(id,params("home").head, params("away").head, params("venue").head,
      params("level").head, "", params("date").head, params("time").head, params("refType").head, params("refFee").head,
      params("assFee").head, params("appointedRef").head, params("appointedAssistant1").head, params("appointedAssistant2").head)
  }

  private def viewAll(req: HttpRequest[_]): Boolean = req.parameterNames.contains("all")
}
