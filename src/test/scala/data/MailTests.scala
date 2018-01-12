package data

import conf.{Config, EmailConfig}
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

    val config = Config("", "", "", "", "", "", EmailConfig("", "", List.empty, None), "", "")

    val text = ClubRefereeNotification(m, config,Some(refUser), None, None).text

    println(text)
  }


}
