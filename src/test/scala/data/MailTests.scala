package data

import conf.{Config, EmailConfig}
import org.constretto.Constretto
import org.constretto.Constretto.json
import org.joda.time.DateTime
import org.scalatest.FunSuite

/**
  *
  */
class MailTests extends FunSuite{

  test("Lager fin plaintext for dommeroppset til klubb"){
    val m = MatchValidation.validate(None, "a-lag", "b-lag", "bortebane", "men3div", "", "2012-10-01", "20:15", "trio", "600", "400", "", "", "",true, "","","","","", "payer@example.com", "away").right.get
      .copy(appointedRef = Some(Referee(null, "Morten Andersen-Gott", Level.Men3Div.key)))

    val refUser = User(None, "","","12345678", "",false, 1, DateTime.now, "")

    System.setProperty("CONSTRETTO_TAGS", "dev")
    val c = Constretto(
      List(
        json("classpath:conf/dev.conf", "config", Some("dev"))
      )
    )[Config]("config")

    val config = Config("", "", "", "", "", "", c.email, "", "")

    val notification = ClubRefereeNotification(m, config, Some(refUser), None, None)

    val text = notification.text

    println(text)

    println(notification.html)
  }


}
