package service

import java.nio.charset.StandardCharsets

import data._

import scala.concurrent.{Await, ExecutionContext}
import scala.util.Properties
import dispatch._
import data.MailMessage
import common._
import conf.Config
import org.joda.time.{LocalDate, LocalDateTime}

import scala.util.Properties

class MailgunService (private val config:Config){
  private val repo = MongoRepository.singletonWithSessionCaching
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = config.email.mailgunAppName orElse
    Properties.envOrNone("MAILGUN_SMTP_LOGIN").map(_.split("@")(1).trim) getOrElse "app15913574.mailgun.org"
  val http = Http.default.closeAndConfigure(config => config.setAcceptAnyCertificate(true))


  def sendAll(messages: List[MailMessage]) : List[MailReceipt]= {
    import scala.concurrent.duration._
    import ExecutionContext.Implicits.global
    val futures : List[Future[MailReceipt]] = messages.map(sendMailAsync)
    val combined = Future.sequence(futures)
    val result = Await.result(combined, 10.seconds)
    println(s"Mail ble sendt med response: ${result}")
    result
  }

  def sendMailAsync(mailMessage: MailMessage) : Future[MailReceipt] = {
    import ExecutionContext.Implicits.global
    val req = mailgunUrl << mailMessage.asMailgunParams
    for {
      resp <-  http(req)
      receipt = if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody) else MailRejected(resp.getResponseBody, resp.getStatusCode)
    } yield receipt
  }

  def sendMail(mail:MailMessage) : MailReceipt = {
    import ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val start = System.currentTimeMillis
    val req = mailgunUrl << mail.asMailgunParams
    val futureReceipt = sendMailAsync(mail)
    val receipt = Await.result(futureReceipt, 10.seconds)
    val end = System.currentTimeMillis
    println(s"Mail receipt (took ${durationSeconds(start, end)}): $receipt")
    receipt
  }

  def refereesAppointed(m: Match) = {
    val ref = m.appointedRef.flatMap(x => repo.userById(x.id))
    val ass1 = m.appointedAssistant1.flatMap(x => repo.userById(x.id))
    val ass2 = m.appointedAssistant2.flatMap(x => repo.userById(x.id))
    val appointmentMail = AppointmentMail(m, config, "", ref, ass1, ass2).toMailMessage
    println(appointmentMail.asMailgunParams)
    val clubNotificationMail = ClubRefereeNotification(m, config, ref, ass1, ass2).toMailMessage
    sendAll(appointmentMail :: clubNotificationMail :: Nil)
      .head
  }

  def sendAppointmentMail(m:Match) = {
    val ref = m.appointedRef.flatMap(x => repo.userById(x.id))
    val ass1 = m.appointedAssistant1.flatMap(x => repo.userById(x.id))
    val ass2 = m.appointedAssistant2.flatMap(x => repo.userById(x.id))
    val mailHelper = AppointmentMail(m, config, "", ref, ass1, ass2)
    val mail = mailHelper.toMailMessage
    sendMail(mail)
  }

  def newMatchEmails(m: MatchTemplate, rootUrl:String, matchUrl:String, matchId:String) : MailReceipt = {
    println("Running: "+LocalDateTime.now)
    val orderConfirmation = OrderConfirmationMail(m, config, rootUrl, matchId)
    val tfdlMail = NewMatchMail(m, matchUrl, config, matchId)
    sendAll(tfdlMail.toMailMessage :: orderConfirmation.toMailMessage :: Nil)
      .head
  }

  def sendMatchOrderEmail(m:MatchTemplate, matchUrl:String, matchId: String) = {
    import m._
    sendMail( NewMatchMail(m, matchUrl, config, matchId).toMailMessage)
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
      url("https://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as_!("api",mailgunApiKey).setBodyEncoding(StandardCharsets.UTF_8).
        POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }

}
