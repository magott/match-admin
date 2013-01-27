package service

import data._
import util.Properties
import dispatch._
import data.MailMessage

object MailgunService {
  private val repo = MongoRepository.singletonWithSessionCaching
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = Properties.envOrElse("MAILGUN_SMTP_LOGIN", "default@app8516420.mailgun.org").split('@')(1)
  val cc = Properties.envOrElse("MAIL_CC", "ofdl@andersen-gott.com")
  val sender = "Oslo Fotballdomerlaug <treningskamper@gmail.com>"

  def sendMail(mail:MailMessage) : MailReceipt = {
    val resp = Http(mailgunUrl << mail.asMailgunParams)()
    if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody)
    else MailRejected(resp.getResponseBody, resp.getStatusCode)
  }

  def sendAppointmentMail(m:Match) = {
    val mailHelper = AppointmentMail(m, cc, "", m.appointedRef.flatMap(x => repo.userById(x.id)), m.appointedAssistant1.flatMap(x => repo.userById(x.id)), m.appointedAssistant2.flatMap(x => repo.userById(x.id)))
    val mail = MailMessage(sender, mailHelper.to, Seq(cc), Nil, mailHelper.subject, mailHelper.text, Some(mailHelper.html))
    sendMail(mail)
  }

  def sendLostpasswordMail(email:String, resetUrl:String) = {
    sendMail( MailMessage(
      sender,
      email,
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
      url("https://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as("api",mailgunApiKey).
        POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }

}
