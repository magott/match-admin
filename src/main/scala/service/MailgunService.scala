package service

import conf.Config
import data._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Properties
import dispatch._
import data.MailMessage
import common._

import scala.util.Properties

class MailgunService (private val config:Config){
  private val repo = MongoRepository.singletonWithSessionCaching
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = config.email.mailgunAppName orElse
    Properties.envOrNone("MAILGUN_SMTP_LOGIN").map(_.split("@")(1).trim) getOrElse "app15913574.mailgun.org"

  def sendMail(mail:MailMessage) : MailReceipt = {
    import ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val start = System.currentTimeMillis
    val req = mailgunUrl << mail.asMailgunParams
    val futureReceipt = for {
      resp <- Http(req)
      receipt = if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody) else MailRejected(resp.getResponseBody, resp.getStatusCode)
    } yield receipt
    val receipt = Await.result(futureReceipt, 10.seconds)
    val end = System.currentTimeMillis
    println(s"Mail receipt (took ${durationSeconds(start, end)}): $receipt")
    receipt
  }

  def sendAppointmentMail(m:Match) = {
    val mailHelper = AppointmentMail(m, config.email.ccOnOrders, "", m.appointedRef.flatMap(x => repo.userById(x.id)), m.appointedAssistant1.flatMap(x => repo.userById(x.id)), m.appointedAssistant2.flatMap(x => repo.userById(x.id)))
    val mail = MailMessage(config.email.fromFdl, mailHelper.to, Seq(config.email.toOnOrders), Nil, mailHelper.subject, mailHelper.text, Some(mailHelper.html))
    sendMail(mail)
  }

  def sendMatchOrderEmail(m:MatchTemplate, matchUrl:String) = {
    import m._
    sendMail(
      MailMessage(
        config.email.fromFdl,
        config.email.toOnOrders,
        config.email.ccOnOrders,
        "Bestilling av dommer",
        s"""Det er bestilt $refereeType til følgende kamp:
          |${kickoff.toString("dd.MM.yyyy HH:mm")}
          |$teams (${Level.asMap(m.level).toString})
          |$venue
          |Regningen betales av ${m.betalendeLag}
          |Regning sendes til ${m.payerEmail}
          |Gå til $matchUrl for å publisere kampen og se mer informasjon om kampen.
        """.stripMargin
      )
    )
  }

  def sendLostpasswordMail(email:String, resetUrl:String) = {
    sendMail( MailMessage(
      config.email.fromFdl,
      email,
      List.empty,
      "Glemt passord",
      """Du mottar denne e-postem fordi du glemt passordet ditt. Det er sånt som skjer.
        |Du kan sette nytt passord ved å gå til %s
        |Passordet må settes innen 30 minutter fra du mottok denne mailen. Dersom du husker passordet ditt eller ikke ønsker å sette nytt passord kan du se bort ifra denne mailen.
        |
        |Mvh,
        |Oslo Fotballdommerlaug
        |""".stripMargin.format(resetUrl)
    ))
  }

    def mailgunUrl = {
      url("https://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as_!("api",mailgunApiKey).
        POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }

}
