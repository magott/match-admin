package web

import unfiltered.request._
import unfiltered.response._
import service.MongoRepository
import org.bson.types.ObjectId
import unfiltered.response.Html5
import unfiltered.response.ResponseString
import data.{Match, Level, Referee}
import java.util.Date
import org.joda.time.DateTime

class AdminHandler {

  val ref = Referee(new ObjectId("50799771040279cbe43df564"), "Morten Andersen-Gott", Level.Men3Div.key)
  val ref2 = Referee(new ObjectId(new Date()), "Fjottlars Trulsen", Level.Girls14.key)
  val refs = ref :: ref2 :: Nil
  val m = Match(None, DateTime.now, "a-lag", "b-lag", "bortebane", "men3div", None, DateTime.now, "trio", Some(600), Some(400), refs, refs, Some(ref), None, None)

  def handleAdmin(req: HttpRequest[_]) = {
    req match {
      case Path("/admin/matches/new") => req match{
        case GET(_) => Html5(Pages(req).editMatchForm(None))
        case POST(_) & Params(p)=>{
          ResponseString(p.map(kv => kv._1 + ": " + kv._2).mkString("\n"))
        }
        case _ => MethodNotAllowed
      }
      case Path(Seg(List("admin","matches", matchId))) => req match{
        case GET(_) =>{
//          MongoRepository.fullMatch(new ObjectId(matchId)) match {
          Option(m) match {
            case None => BadRequest ~> Html5(Pages(req).notFound(Some("Ingen kamp med id " + matchId)))
            case Some(m) => Html5(Pages(req).editMatchForm(Some(m)))
          }
        }
        case POST(_) & Params(p)=>{
          ResponseString(p.map(kv => kv._1 + ": " + kv._2).mkString("\n"))
        }
      }
      case _ => NotFound ~> Html5(Pages(req).notFound(Some("Ukjent adminside")))
    }
  }
}
