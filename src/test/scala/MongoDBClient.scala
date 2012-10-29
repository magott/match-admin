import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import common.MongoSetting
import data.Match
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.mongodb.casbah.Imports._

object MongoDBClient extends App{

  RegisterJodaTimeConversionHelpers()
  val MongoSetting(db) = None

  val m = Match(Some(new ObjectId("50899d9e23e49a26b5d09ed4")), DateTime.now, "hjemme", "borte", "der","men4div", None, DateTime.now, "dommer", Some(100), Some(50), Nil, Nil, None, None, None)

//  db("matches").update(o=m.updateClause, q=m.copy(homeTeam="himme").toMongo, upsert=true, multi=false)
  db("matches").update(q=MongoDBObject("_id" -> new ObjectId("50899d9e23e49a26b5d09ed4")), o=MongoDBObject("homeTeam" -> "himme"), upsert=true, multi=false)

//    db("foo").update(o=MongoDBObject("_id" -> ), q=)

}
