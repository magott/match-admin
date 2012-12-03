package service

import common.MongoSetting
import util.Properties
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import data.{Session, Referee, User, Match}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.{DateMidnight, DateTime}

object MongoRepository {

  RegisterJodaTimeConversionHelpers()

  val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")

  private val where, has, by = MongoDBObject

  def fullMatch(objId: ObjectId) = {
    db("matches").findOne(where("_id" -> objId)).map(Match.fromMongo(_))
  }

  def saveMatch(m: Match) = {
    db("matches").update(q = m.updateClause, o= m.toMongo, upsert=true, multi=false)
  }

  def deleteMatch(matchId: ObjectId) = {
    val foo = db("matches").findAndRemove(where("_id" -> matchId))
    foo
  }

  def listUpcomingMatches = {
    listMatchesNewerThan(DateMidnight.now.toDateTime)
  }

  def listMatchesNewerThan(date:DateTime) = {
    db("matches").find( ("kickoff" $gt date)).sort(by("kickoff" -> 1)).map(Match.fromMongo(_))
  }

  def assistantInterestedInMatch(matchId: ObjectId, userId:ObjectId) = {
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intAss" -> Referee.fromUser(user).toMongo)).getN == 1
  }

  def refInterestedInMatch(matchId: ObjectId, userId:ObjectId):Boolean = {
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intRef" -> Referee.fromUser(user).toMongo)).getN == 1
  }

  def hasDeclaredInterestAsAssistant(matchId:ObjectId, refId: ObjectId):Boolean = {
    db("matches").findOne(where("_id" -> matchId, "intAss._id" -> refId)).isDefined
  }

  def hasDeclaredInterestAsReferee(matchId:ObjectId, refId: ObjectId):Boolean = {
    db("matches").findOne(where("_id" -> matchId, "refAss._id" -> refId)).isDefined
  }

  def cancelInterestAsReferee(matchId:ObjectId, refId:ObjectId):Boolean = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intRef" -> has("_id" -> refId))), upsert=true, multi=true).getN > 0
  }

  def cancelInterestAsAssistant(matchId:ObjectId, refId:ObjectId):Boolean = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intAss" -> has("_id" -> refId))), upsert=true, multi=true).getN > 0
  }

  def userForSession(sessionId:String) : Option[User]= {
    sessionById(sessionId).flatMap(s => db("users").findOne(where("_id" -> s.userId)).map(User.fromMongo))
  }

  def refereeByUserId(userId:ObjectId) : Option[Referee] = userById(userId).map(Referee.fromUser)

  def userById(userId:ObjectId) : Option[User] = db("users").findOne(where("_id" -> userId)).map(User.fromMongo)

  def sessionById(sessionId:String) : Option[Session] = {
    val session = db("sessions").findOne(where("sessionId"->sessionId)).map(Session.fromMongo)
    session
  }

  def allUsers = db("users").find().map(User.fromMongo).toList

  def newSession(session:Session){
    db("sessions").save(session.toMongo)
  }

  def updateUserSessions(email:String, user:User) = {
    db("sessions").update(q=where("username" -> email), o= $set("username" -> user.email, "name" -> user.name, "admin" -> user.admin), upsert=false, multi=true)
  }

  def saveUser(user:User) = {
    db("users").update(q = user.updateClause, o = user.toMongo, upsert = true, multi = false)
    userByEmail(user.email)
  }

  def userByEmail(email:String)= {
    db("users").findOne(where("email" -> email)).map(User.fromMongo)
  }


}
