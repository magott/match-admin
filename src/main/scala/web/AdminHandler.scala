package web

import conf.Config
import unfiltered.request._
import unfiltered.response._
import service.MongoRepository._
import org.bson.types.ObjectId
import unfiltered.response.Html5
import unfiltered.response.ResponseString
import data._
import java.util.Date

import org.joda.time.{DateMidnight, DateTime, LocalDate}
import service.{MailgunService, MatchService, MongoRepository}

import scala.Left
import data.MailAccepted

import scala.Right
import scala.Some
import unfiltered.response.Html5
import unfiltered.response.ResponseString
import org.joda.time
import unfiltered.request.Params.Extract

class AdminHandler (private val matchService:MatchService, private val mailgun:MailgunService) (implicit val config:Config){

  val mongo = matchService.repo


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
            Html5(Pages(req).adminMatchListView(mongo.listPublishedMatchesNewerThan(new DateMidnight(2000,1,1).toDateTime)))
          else
            Html5(Pages(req).adminMatchListView(mongo.listPublishedMatchesNewerThan(seasonStart)))
        }
        case POST(_) & Params(p) & LoggedOnUser(user)=>{
          matchFromParams(None, p) match{
            case Left(errors) => Html5(Pages(req).errorPage(errors.map(e => <p>{e}</p>)))
            case Right(m) => {
              matchService.saveMatch(m, user)
              HerokuRedirect(req, "/admin/matches")
            }
          }
        }
      }
      case Path(Seg("admin" :: "matches" :: matchId :: "sendmail" :: Nil)) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case _ => {
          mongo.fullMatch(new ObjectId(matchId)) match{
            case Some(m) => mailgun.refereesAppointed(m) match{
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
      case Path(Seg("admin" :: "matches" :: matchId :: "admin-status" :: Nil)) & Params(p)=> {
        p.get("from-state").map(from => if (from.head == "open"){
          mongo.markMatchAsDone(new ObjectId(matchId))
          Ok ~> JsonContent ~> ResponseString("""{"newState":"done"}""")
        }else{
          mongo.markMatchAsOpen(new ObjectId(matchId))
          Ok ~> JsonContent ~> ResponseString("""{"newState":"open"}""")
        }
        ).getOrElse(BadRequest)
      }
      case Path(Seg("admin" :: "users" :: "search" :: Nil)) =>  req match {
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => req match{
          case Params(NameParam(name)) => {
            Ok ~> JsonContent ~> ResponseString(mongo.searchUserByName(name).map(User.toIdNameJson).mkString("[",",","]"))
          }
          case _ => BadRequest ~> ResponseString("""'name' param is required""")
        }
        case _ => MethodNotAllowed
      }

      case Path(Seg("admin" :: "users" :: Nil)) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) => Ok ~> Html5(Pages(req).userList(mongo.allUsers))
      }
      case Path(Seg(List("admin", "users", userId))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>
          mongo.userById(new ObjectId(userId)) match{
            case Some(user) => Ok ~> Html5(Pages(req).user(user, mongo.matchesWithReferee(user.id.get), "/admin/matches/"))
            case None => NotFound ~> Html5(Pages(req).notFound(Some("Fant ingen dommer med id %s".format(userId))))
          }
        case _ => MethodNotAllowed
      }
      case Path(Seg("admin" :: "matches" :: "orders" :: Nil)) => req match{
        case GET(_) => {
          val unpublishedMatches = mongo.listUnpublishedMatches
          Ok ~> Html5(Pages(req).unpublishedMatches(unpublishedMatches))
        }
      }
      case Path(Seg(List("admin","matches", matchId))) => req match{
        case NotAdmin(_) => Forbidden ~> Html5(Pages(req).forbidden)
        case GET(_) =>{
          mongo.fullMatch(new ObjectId(matchId)) match {
            case None => BadRequest ~> Html5(Pages(req).notFound(Some("Ingen kamp med id " + matchId)))
            case Some(m) => Html5(Pages(req).editMatchForm(Some(m)))
          }
        }
        case POST(_) & Params(p)=> req match {
          case Params(RefTypeParam(reftype)) & Params(UserIdParam(userid)) => req match{
            case Params(RefTypeParam("ref")) => {
              mongo.refInterestedInMatch(new ObjectId(matchId), new ObjectId(userid))
              NoContent
            }
            case Params(RefTypeParam("assRef")) => {
              mongo.assistantInterestedInMatch(new ObjectId(matchId), new ObjectId(userid))
              NoContent
            }
            case _ => BadRequest~> JsonContent ~> ResponseString("""{"error":"Invalid or no reftype specified" }""")
          }
          case LoggedOnUser(user) => matchFromParams(Some(matchId), p) match{
            case Left(errors) => Html5(Pages(req).errorPage(errors.map(e => <p>{e}</p>)))
            case Right(m) => {
              matchService.saveMatch(m,user)
              HerokuRedirect(req, "/admin/matches")
            }
          }
        }
        case DELETE(_) => {
          mongo.deleteMatch(new ObjectId(matchId))
          Ok ~> JsonContent ~> ResponseString("""{"href": "/admin/matches"}""")
        }
      }
      case _ => NotFound ~> Html5(Pages(req).notFound(Some("Ukjent adminside")))
    }
  }


  def seasonStart: DateTime = {
    val novemberFirst = DateTime.now.withTimeAtStartOfDay().withMonthOfYear(11).withDayOfMonth(1)
    if(novemberFirst.isAfterNow) novemberFirst.minusYears(1) else novemberFirst
  }

  def matchFromParams(id:Option[String], p:Map[String, Seq[String]]) = {
    val params = p.withDefaultValue(List(""))
    MatchValidation.validate(id,params("home").head, params("away").head, params("venue").head,
      params("level").head, "", params("date").head, params("time").head, params("refType").head, params("refFee").head,
      params("assFee").head, params("appointedRef").head, params("appointedAssistant1").head, params("appointedAssistant2").head,
      params("saveContact").head == "on", params("clubContactName").head, params("clubContactTelephone").head,
      params("clubContactAddress").head, params("clubContactZip").head, params("clubContactEmail").head,
      params("payerEmail").head, params("payingTeam").head)
  }

  private def viewAll(req: HttpRequest[_]): Boolean = req.parameterNames.contains("all")

  object NameParam extends Params.Extract("term", Params.first)
  object UserIdParam extends Params.Extract("userid", Params.first)
  object RefTypeParam extends Params.Extract("reftype", Params.first)



}
