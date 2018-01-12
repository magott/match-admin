package data

import org.scalatest.FunSuite
import org.joda.time.{LocalTime, Instant, DateMidnight}

class MatchValidationTest extends FunSuite{

  test("Invalid match"){
    val v = MatchValidation.validate(None, "a-lag", "b-lag", "bortebane", "men3div", "", "2-2-2-2", "20:15", "dommer", "600", "", "", "", "", true, "","","","","", "payer@example.com", "away").left.map(_.toSet)
    assert(v === Left(Set("2-2-2-2 er ikke et gyldig datoformat (yyyy-MM-dd)")))
  }

  test("Valid match"){
    val v = MatchValidation.validate(None, "a-lag", "b-lag", "bortebane", "men3div", "", "2012-10-01", "20:15", "trio", "600", "400", "", "", "",true, "","","","","", "payer@example.com", "away")
    assert(v.isRight)
  }

  test("Invalid matchtemplate, wrong dateformat") {
    val mt = MatchValidation.unpublished("home", "away", "venuue", "men3div", "20150329", "11:00", "trio", "name", "12345678", "Adresse", "1358", "email@mail.com", "home", "payer@home.com")
    assert(mt.isLeft)
  }
}
