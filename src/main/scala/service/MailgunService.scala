package service

import data._
import util.Properties
import dispatch._
import data.MailMessage

object MailgunService {
  val mailgunApiKey = Properties.envOrNone("MAILGUN_API_KEY").get
  val mailgunAppName = Properties.envOrElse("MAILGUN_SMTP_LOGIN", "default@app8516420.mailgun.org").split('@')(1)
  val cc = Properties.envOrElse("MAIL_CC", "ofdl@andersen-gott.com")

  def sendMail(mail:MailMessage) : MailReceipt = {
    val resp = Http(mailgunUrl << mail.asMailgunParams)()
    if(resp.getStatusCode == 200) MailAccepted(resp.getResponseBody)
    else MailRejected(resp.getResponseBody, resp.getStatusCode)
  }

  def sendAppointmentMail(m:Match) = {
    import MongoRepository._
    val mailHelper = AppointmentMail(m, cc, "", m.appointedRef.flatMap(x => userById(x.id)), None, None)
    val mail = MailMessage(mailHelper.from, mailHelper.to, Seq(cc), Nil, mailHelper.subject, mailHelper.text, Some(mailHelper.html))
    sendMail(mail)
  }

    def mailgunUrl = {
      url("https://api.mailgun.net/v2/%s/messages".format(mailgunAppName)).as("api",mailgunApiKey).
        POST <:< (Map("Content-Type" -> "application/x-www-form-urlencoded"))
  }

}
