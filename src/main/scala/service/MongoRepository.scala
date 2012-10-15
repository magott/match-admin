package service

import common.MongoSetting
import util.Properties
import org.bson.types.ObjectId
import com.mongodb.casbah.query.Imports._
import data.{Session, Referee, User, Match}

object MongoRepository {

  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  //

  private val where, has = MongoDBObject

  def fullMatch(objId: ObjectId) = {
    db("matches").findOne(where("_id" -> objId)).map(Match.fromMongo(_))
  }

  def saveMatch(m: Match) = {
    db("matches").update(o = m.updateClause, q= m.toMongo, upsert=true, multi=false)
  }

  def assistantInterestedInMatch(matchId: ObjectId, userId:ObjectId, refereeType:String){
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intAss" -> Referee.fromUser(user)))
  }

  def refInterestedInMatch(matchId: ObjectId, userId:ObjectId, refereeType:String){
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intRef" -> Referee.fromUser(user)))
  }

  def hasDeclaredInterestAsAssistant(matchId:ObjectId, refId: ObjectId):Boolean = {
    db("matches").findOne(where("_id" -> matchId, "intAss._id" -> refId)).isDefined
  }

  def hasDeclaredInterestAsReferee(matchId:ObjectId, refId: ObjectId):Boolean = {
    db("matches").findOne(where("_id" -> matchId, "refAss._id" -> refId)).isDefined
  }

  def cancelInterestAsReferee(matchId:ObjectId, refId:ObjectId) = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intRef" -> has("_id" -> refId))), upsert=true, multi=true)
  }

  def cancelInterestAsAssistant(matchId:ObjectId, refId:ObjectId) = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intAss" -> has("_id" -> refId))), upsert=true, multi=true)
  }

  def userForSession(sessionId:String) : Option[User]= {
    sessionById(new ObjectId(sessionId)).flatMap(s => db("users").findOne(where("_id" -> s.userId)).map(User.fromMongo))
  }

  def sessionById(sessionId:ObjectId) : Option[Session] = {
    db("sessions").findOne(where("sessionId"->sessionId)).map(Session.fromMongo)
   }

  private def byId(id:Option[ObjectId]) = if(id.isDefined) where("_id"->id.get) else MongoDBObject.empty


}
