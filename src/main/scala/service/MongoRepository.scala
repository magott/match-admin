package service

import common.MongoSetting
import scala.util.Properties
import com.mongodb.casbah.query.Imports._
import data._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.{DateMidnight, DateTime}
import com.mongodb.casbah.{WriteConcern, MongoDB}
import org.bson.types.ObjectId

class MongoRepository(db:MongoDB) extends SessionRepository{


  private val where, has, by = MongoDBObject

  def fullMatch(objId: ObjectId) = {
    val maybeMatch = db("matches").findOne(where("_id" -> objId)).map(Match.fromMongo(_))
    maybeMatch
  }

  def saveMatch(m: Match) = {
    if(m.id.isDefined)
      db("matches").update(q = m.updateClause, o= m.asUpdate, upsert=false, multi=false)
    else
      db("matches").save(o=m.asInsert)
  }

  def deleteMatch(matchId: ObjectId) = {
    val foo = db("matches").findAndRemove(where("_id" -> matchId))
    foo
  }

  def listUpcomingMatches : Seq[Match] = {
    listPublishedMatchesNewerThan(DateMidnight.now.toDateTime)
  }

  def listUnpublishedMatches : Seq[Match]= {
    val unpublished = where("published" -> false)
    db("matches").find( unpublished ).sort(by("kickoff" -> 1)).map(Match.fromMongo(_)).toSeq
  }

  def saveNewMatchTemplate(m:MatchTemplate) = {
    val mongoObject = m.toMongo
    db("matches").save(mongoObject)
    mongoObject.as[ObjectId]("_id")
  }

  def listPublishedMatchesNewerThan(date:DateTime) : Seq[Match]= {
    val afterDate = ("kickoff" $gt date)
    val notUnpublished = ("published" $ne false)
    db("matches").find( afterDate ++ notUnpublished ).sort(by("kickoff" -> 1)).map(Match.fromMongo(_)).toSeq
  }

  def markMatchAsDone(matchId:ObjectId) = {
    db("matches").update(q= where("_id" -> matchId), o= $set("adminOk" -> true))
  }

  def markMatchAsOpen(matchId:ObjectId) = {
    db("matches").update(q= where("_id" -> matchId), o= $set("adminOk" -> false))
  }

  def assistantInterestedInMatch(matchId: ObjectId, userId:ObjectId) = {
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intAss" -> Referee.fromUser(user).toMongo), concern = WriteConcern.Acknowledged).getN > 0
    db("matches").update(q= where("_id" -> matchId), o= $addToSet("intAss" -> Referee.fromUser(user).toMongo), concern = WriteConcern.Acknowledged).getN > 0
  }

  def refInterestedInMatch(matchId: ObjectId, userId:ObjectId):Boolean = {
    val user = User.fromMongo(db("users").findOneByID(userId).get)
    val updated = db("matches").update(q = where("_id" -> matchId), o = $addToSet("intRef" -> Referee.fromUser(user).toMongo), concern = WriteConcern.Acknowledged)
      .getN
    updated == 1
  }

  def cancelInterestAsReferee(matchId:ObjectId, refId:ObjectId):Boolean = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intRef" -> has("_id" -> refId))), upsert=true, multi=true, concern = WriteConcern.Acknowledged).getN > 0
  }

  def cancelInterestAsAssistant(matchId:ObjectId, refId:ObjectId):Boolean = {
//    db("foo").update(q= where("_id" -> id), o= $pull(where("number" -> MongoDBObject("num" -> 2))))
    db("matches").update(q= where("_id" -> matchId), o= $pull(where("intAss" -> has("_id" -> refId))), upsert=true, multi=true, concern = WriteConcern.Acknowledged).getN > 0
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

  def matchesWithReferee(refereeId:ObjectId) = {
    val refereeOrAssistantQuery = $or("referee._id" -> refereeId, "assRef1._id" -> refereeId, "assRef2._id" -> refereeId)
    db("matches").find(refereeOrAssistantQuery).map(Match.fromMongo(_)).toList
  }

  def allUsers = db("users").find().map(User.fromMongo).toList

  def newSession(session:Session){
    db("sessions").save(session.toMongo)
  }

  def updateUserSessions(email:String, user:User) = {
    val setting = $set("username" -> user.email, "name" -> user.name, "admin" -> user.admin)
    db("sessions").update(q=where("username" -> email), o= setting, upsert=false, multi=true)
  }

  def saveUser(user:User) : Option[User] = {
    if(user.id.isDefined)
      db("users").update(q = user.updateClause, o = user.toMongo, upsert = false, multi = false)
    else
      db("users").save(user.toMongo)
    userByEmail(user.email)
  }

  def userByEmail(email:String)= {
    db("users").findOne(where("email" -> email.toLowerCase)).map(User.fromMongo)
  }

  def searchUserByName(partial:String) = {
    db("users").find(where("name" -> s"(?i)$partial".r)).map(User.fromMongo)
  }

}

object MongoRepository{
  RegisterJodaTimeConversionHelpers()
  private val MongoSetting(db) = Properties.envOrNone("MONGOLAB_URI")
  private val singleton = new MongoRepository(db) with CachingSessionRepository
  def singletonWithSessionCaching:MongoRepository = singleton
}
