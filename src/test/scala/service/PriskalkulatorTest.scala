package service

import data.Match
import org.bson.types.ObjectId
import org.joda.time.{DateTime, Days, LocalDateTime, Weeks}
import org.scalatest.FunSuite

class PriskalkulatorTest extends FunSuite{

  test("Lager korrekte regningslinjer 2019"){

    val dec_1_2019 = new DateTime(2019, 12, 1, 12, 0)

    val senTrio = Match(Some(new ObjectId(dec_1_2019.toDate)), DateTime.now, "hjemme", "borte", "der","men4div", None, dec_1_2019.plusDays(6),
      "trio", Some(100), Some(50), Nil, Nil, None, None, None, true, true, None, "payer@example.com", Some("home"), None)
    val tidligTrio = senTrio.copy(kickoff = dec_1_2019.plusWeeks(1))
    val hasteTrio = senTrio.copy(kickoff = dec_1_2019.plusHours(48))

    val hasteDommer = hasteTrio.copy(refereeType = "dommer")
    val senDommer = senTrio.copy(refereeType = "dommer")
    val tidligDommer = tidligTrio.copy(refereeType = "dommer")

    assert(PrisKalkulator.varelinje(hasteTrio).productCode == "8")
    assert(PrisKalkulator.varelinje(hasteTrio).unitPrice == 350)
    assert(PrisKalkulator.varelinje(senTrio).productCode == "3")
    assert(PrisKalkulator.varelinje(senTrio).unitPrice == 250)
    assert(PrisKalkulator.varelinje(tidligTrio).productCode == "1")
    assert(PrisKalkulator.varelinje(tidligTrio).unitPrice == 150)

    assert(PrisKalkulator.varelinje(hasteDommer).productCode == "9")
    assert(PrisKalkulator.varelinje(hasteDommer).unitPrice == 300)
    assert(PrisKalkulator.varelinje(senDommer).productCode == "4")
    assert(PrisKalkulator.varelinje(senDommer).unitPrice == 200)
    assert(PrisKalkulator.varelinje(tidligDommer).productCode == "2")
    assert(PrisKalkulator.varelinje(tidligDommer).unitPrice == 100)
  }

  test("Lager korrekte regningslinjer 2018"){

    val dec_1_2019 = new DateTime(2018, 10, 15, 12, 0)

    val senTrio = Match(Some(new ObjectId(dec_1_2019.toDate)), DateTime.now, "hjemme", "borte", "der","men4div", None, dec_1_2019.plusDays(6),
      "trio", Some(100), Some(50), Nil, Nil, None, None, None, true, true, None, "payer@example.com", Some("home"), None)
    val tidligTrio = senTrio.copy(kickoff = dec_1_2019.plusWeeks(1))
    val hasteTrio = senTrio.copy(kickoff = dec_1_2019.plusHours(48))

    val hasteDommer = hasteTrio.copy(refereeType = "dommer")
    val senDommer = senTrio.copy(refereeType = "dommer")
    val tidligDommer = tidligTrio.copy(refereeType = "dommer")

    assert(PrisKalkulator.varelinje(hasteTrio).productCode == "3")
    assert(PrisKalkulator.varelinje(hasteTrio).unitPrice == 250)
    assert(PrisKalkulator.varelinje(senTrio).productCode == "3")
    assert(PrisKalkulator.varelinje(senTrio).unitPrice == 250)
    assert(PrisKalkulator.varelinje(tidligTrio).productCode == "1")
    assert(PrisKalkulator.varelinje(tidligTrio).unitPrice == 150)

    assert(PrisKalkulator.varelinje(hasteDommer).productCode == "4")
    assert(PrisKalkulator.varelinje(hasteDommer).unitPrice == 200)
    assert(PrisKalkulator.varelinje(senDommer).productCode == "4")
    assert(PrisKalkulator.varelinje(senDommer).unitPrice == 200)
    assert(PrisKalkulator.varelinje(tidligDommer).productCode == "2")
    assert(PrisKalkulator.varelinje(tidligDommer).unitPrice == 100)
  }



}
