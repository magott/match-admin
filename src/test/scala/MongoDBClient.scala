import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import common.MongoSetting
import data.Match
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.mongodb.casbah.Imports._
import service.MongoRepository

object MongoDBClient extends App{

  RegisterJodaTimeConversionHelpers()
  val MongoSetting(db) = None

//  val m = Match(Some(new ObjectId("50899d9e23e49a26b5d09ed4")), DateTime.now, "hjemme", "borte", "der","men4div", None, DateTime.now,
//    "dommer", Some(100), Some(50), Nil, Nil, None, None, None, true, true, None)

  val m = Match(None, DateTime.now, "hjemme", "borte", "der","men4div", None, DateTime.now,
    "dommer", Some(100), Some(50), Nil, Nil, None, None, None, true, true, None)

  db("matches").update(q=MongoDBObject("foo" -> "bar"), o=m.asUpdate, upsert=true, multi=false)
//  db("matches").update(q=MongoDBObject("_id" -> new ObjectId("50899d9e23e49a26b5d09ed4")), o=MongoDBObject("homeTeam" -> "himme"), upsert=true, multi=false)

//  db("foo").update(q=MongoDBObject(), o=MongoDBObject("fuck"->"face"), upsert=true, multi=false)
//  db("foo").save(o=MongoDBObject("fuck"->"face"))

  val repo = MongoRepository.singletonWithSessionCaching
//  val matches = repo.matchesWithReferee(new ObjectId("50a55c66bbb2090ff8c3b3f7"))
  val matches = repo.listPublishedMatchesNewerThan(DateTime.now.minusYears(2))
      matches.foreach(println)

}
