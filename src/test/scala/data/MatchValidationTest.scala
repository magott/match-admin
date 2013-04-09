package data

import org.scalatest.FunSuite
import org.joda.time.{LocalTime, Instant, DateMidnight}

class MatchValidationTest extends FunSuite{

  test("Invalid match"){
    val v = MatchValidation.validate(None, "a-lag", "b-lag", "bortebane", "men3div", "", "2-2-2-2", "20:15", "dommer", "600", "", "", "", "", true, "","","","","").left.map(_.toSet)
    assert(v === Left(Set("2-2-2-2 er ikke et gyldig datoformat (yyyy-MM-dd)")))
  }

  test("Valid match"){
    val v = MatchValidation.validate(None, "a-lag", "b-lag", "bortebane", "men3div", "", "2012-10-01", "20:15", "trio", "600", "400", "", "", "",true, "","","","","")
    assert(v.isRight)
  }
}
