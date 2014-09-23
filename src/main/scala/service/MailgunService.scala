package service

import conf.Config
import data._
import scala.concurrent.{Await, ExecutionContext}
import util.Properties
import dispatch._
import data.MailMessage

import scala.util.Properties

class MailgunService (private val config:Config){
  private val repo = MongoRepository.singletonWithSessionCaching
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = Properties.envOrElse("MAILGUN_SMTP_LOGIN", "postmaster@app15913574.mailgun.org").split('@')(1).trim
  val cc = Properties.envOrElse("MAIL_CC", "ofdl@andersen-gott.com")
  val sender = "Oslo Fotballdomerlaug <treningskamper@gmail.com>"

  def sendMail(mail:MailMessage) : MailReceipt = {
    import ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val resp = Await.result(Http(mailgunUrl << mail.asMailgunParams), 5.seconds)
    if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody)
    else MailRejected(resp.getResponseBody, resp.getStatusCode)
  }

  def sendAppointmentMail(m:Match) = {
    val mailHelper = AppointmentMail(m, config.email.ccOnOrders, "", m.appointedRef.flatMap(x => repo.userById(x.id)), m.appointedAssistant1.flatMap(x => repo.userById(x.id)), m.appointedAssistant2.flatMap(x => repo.userById(x.id)))
    val mail = MailMessage(sender, mailHelper.to, Seq(cc), Nil, mailHelper.subject, mailHelper.text, Some(mailHelper.html))
    sendMail(mail)
  }

  def sendMatchOrderEmail(m:MatchTemplate, matchUrl:String) = {
    sendMail(
      MailMessage(
        config.email.fromFdl,
        config.email.toOnOrders,
        List.empty,
        "Bestilling av dommer",
        """Det er bestilt dommer til følgende kamp:
          |%s
          |%s (%s)
          |%s
          |Gå til %s for å publisere kampen og se mer informasjon om kampen.
        """.stripMargin.format(m.kickoff.toString("dd.MM.yyyy HH:mm"), m.teams, Level.asMap(m.level).toString, m.venue, matchUrl)
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
      url("http://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as_!("api",mailgunApiKey).
        POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }

}
