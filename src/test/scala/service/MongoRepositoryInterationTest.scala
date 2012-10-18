package service

import org.scalatest.FunSuite
import data.Match
import org.joda.time.DateTime
import org.bson.types.ObjectId
import java.util.Date
import com.mongodb.casbah.query.Imports._
import common.MongoSetting
import scala.Some
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

class MongoRepositoryInterationTest extends FunSuite{
  val MongoSetting(db) = None
  val where = MongoDBObject

  val m = Match(None, DateTime.now,"home","away","venue","level",Some("desc"),DateTime.now.plusDays(7),"trio",None,None,Nil,Nil,None,None,None)

  test("Foo"){
    val mongo = m.toMongo
    println(mongo.toString)
  }

  test("Insert new match"){
    RegisterJodaTimeConversionHelpers()
    val m1 = m.copy(homeTeam = "BLAH", description=None)
    db("a").update(m1.updateClause, m.toMongo, true, false)
  }

  test("Multiple inserts"){
    val m1 = m
//    val id = MongoRepository.saveMatch(m)
    val int = m1.interestedAssistants ::: List(new ObjectId(new Date,3,3))
//    val m2 = m1.copy(id = Some(id), interestedAssistants = int, description = None)
  }

  test("Add to set"){

    val bObject = $addToSet("numberLiterals") $each(List("one", "two"))
    val data = MongoDBObject("test" -> "test") ++ bObject
    db("test").update(MongoDBObject.empty, bObject, true, false)
  }

  test("Toggle off") {
    val MongoSetting(db) = None
    val id = new ObjectId("50773262b8c9ae1683d02e4a")
    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
  }

  test("Toggle on") {
    val MongoSetting(db) = None
    val id = new ObjectId("50773262b8c9ae1683d02e4a")
    db("foo").update(q= where("_id" -> id), o= $addToSet("number" -> MongoDBObject("num" ->10, "lit" -> "ti")))
  }

  test("Exists"){
    val MongoSetting(db) = None
//    val exists = db("foo").exists(_.getAs[List[Int]]("number.num").exists(_ == 1))
    val exists = db("foo").findOne(MongoDBObject("name" -> "Morten", "number.lit" -> "en"))
    println(exists)
  }


}
