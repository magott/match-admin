package web

import conf.Config
import org.joda.time.Period
import service.{MailgunService, MongoRepository}
import unfiltered.request._
import unfiltered.response._
import unfiltered.response.Html5
import data.{MatchTemplate, MatchValidation}
import unfiltered.request.UserAgent
import common._

class ClubHandler(repo:MongoRepository, mailgun:MailgunService) (implicit val config:Config){

  def handleClubRequest(req: HttpRequest[_]) = {
    req match{
      case Path(Seg("clubs"::"matches"::"new"::Nil)) => req match {
        case GET(_) => Ok ~> Html5(Pages(req).clubNewMatch)
        case POST(_) => {
          val Params(p) = req
          handleNewMatchFromClub(p) match {
            case Right(m) => {
              val before = System.currentTimeMillis
              val matchId = repo.saveNewMatchTemplate(m)
              val storedAt = System.currentTimeMillis
              val editMatchUrl = rootUrl(req) + "/admin/matches/"+matchId
              val UserAgent(ua) = req
              println("New match from club (%s) : %s [UA:%s] (stored in %s s)".format(matchId, m.toString, ua, durationSeconds(storedAt, before)))
              mailgun.sendMatchOrderEmail(m, editMatchUrl)
              val after = System.currentTimeMillis
              println(s"Processing new match took ${durationSeconds(before, after)}")
              Ok ~> Html5(Pages(req).refereeOrderReceipt(m))
            }
            case Left(errors) => {
              printErrors(errors, req)
              BadRequest ~> Html5(Pages(req).newMatchError(errors))
            }
          }
       }
      }
      case _ => NotFound ~> Html5(Pages(req).notFound())
    }
  }

  def printErrors(errors:List[String], req:HttpRequest[_]) = {
    val UserAgent(ua) = req
    println("400 - User [%s] errors %s".format(ua, errors))
  }

  def handleNewMatchFromClub(params: Map[String, Seq[String]]) : Either[List[String], MatchTemplate] = {
    val p = params.withDefaultValue(List(""))
    println("New match posted with params "+params.mkString)
    MatchValidation.unpublished(p("home").head, p("away").head, p("venue").head, p("level").head, p("date").head,
                                p("time").head, p("refType").head, p("clubContactName").head, p("clubContactTelephone").head,
                                p("clubContactAddress").head, p("clubContactZip").head, p("clubContactEmail").head)
  }

  def rootUrl(req: HttpRequest[_]) = {
    req match {
      case XForwardProto(_) & Host(host) => "https://%s".format(host)
      case Host(host) => "http://%s".format(host)
    }
  }

}
