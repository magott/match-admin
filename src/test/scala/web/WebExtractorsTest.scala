package web

import org.scalatest.FunSuite
import data.Session
import org.bson.types.ObjectId
import java.util.Date
import org.joda.time.DateTime

class WebExtractorsTest  extends FunSuite{

  test("Admin Session"){
    val adminSession = Session(new ObjectId(new Date), "foo", "bar", true, "123", DateTime.now.plusHours(1))

  }


}
