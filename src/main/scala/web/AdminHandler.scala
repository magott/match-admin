package web

import unfiltered.request._
import unfiltered.response._
import service.MongoRepository._
import org.bson.types.ObjectId
import unfiltered.response.Html5
import unfiltered.response.ResponseString
import data.{MatchValidation, Level, Referee}
import java.util.Date
import org.joda.time.DateTime

class AdminHandler {

  val ref = Referee(new ObjectId("50799771040279cbe43df564"), "Morten Andersen-Gott", Level.Men3Div.key)
  val ref2 = Referee(new ObjectId(new Date()), "Fjottlars Trulsen", Level.Girls14.key)
  val refs = ref :: ref2 :: Nil

  def handleAdmin(req: HttpRequest[_]) = {
    req match {
      case Path("/admin/matches/new") => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => Html5(Pages(req).editMatchForm(None))
        case _ => MethodNotAllowed
      }
      case Path(Seg(List("admin", "matches"))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => Html5(Pages(req).listMatches(listMatchesNewerThan(DateTime.now.withDayOfYear(1)), "/admin/matches/"))
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
      case Path(Seg(List("admin", "users", userId))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>
          userById(new ObjectId(userId)) match{
            case Some(user) => Ok ~> Html5(Pages(req).user(user))
            case None => NotFound ~> Html5(Pages(req).notFound(Some("Fant ingen dommer med id %s".format(userId))))
          }
        case _ => MethodNotAllowed
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
}
