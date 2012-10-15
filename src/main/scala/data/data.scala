package data

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.mongodb.casbah.query.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import java.util.UUID
import collection.mutable

//Referees interested
//Match - id, assistant, ref,

//Match list view
//Match without interested parties

//Referee detailed match view
//Match with ids to check if interested.

//Full admin view
case class Match(id:Option[ObjectId], created:DateTime, homeTeam:String, awayTeam:String, venue:String, level:String,
                 description:Option[String], kickoff:DateTime, refereeType:String, refFee:Option[Int],
                 assistantFee:Option[Int], interestedRefs:List[Referee], interestedAssistants:List[Referee],
                 appointedRef:Option[Referee], appointedAssistant1:Option[Referee], appointedAssistant2: Option[Referee]){

  def toMongo: MongoDBObject  = {
    RegisterJodaTimeConversionHelpers()
//    val builder = MongoDBObject.newBuilder
//    id.foreach(builder += "_id" -> _)
//    val update = builder.result()
//    update ++

    val unsets = mutable.MutableList.empty[String]

    val sets = mutable.Map[String, Any](
      "created" -> created,
      "homeTeam" -> homeTeam,
      "awayTeam" -> awayTeam,
      "venue" -> venue,
      "level" -> level,
      "kikcoff" -> kickoff,
      "refereeType" -> refereeType
    )
    description.foreach(x => sets += "desc" -> x)
    refFee.foreach(x => sets += "refFee" -> x)
    assistantFee.foreach(x => sets += "assFee" -> x)
    appointedRef.foreach(x => sets += "referee" -> x.toMongo)
    appointedAssistant1.foreach(x => sets += "assRef1" -> x.toMongo)
    appointedAssistant2.foreach(x => sets += "assRef2" -> x.toMongo)

    if(description.isEmpty) unsets += "desc"
    if(refFee.isEmpty) unsets += "refFee"
    if(assistantFee.isEmpty) unsets += "assFee"
    if(appointedRef.isEmpty) unsets += "referee"
    if(appointedAssistant1.isEmpty) unsets += "assRef1"
    if(appointedAssistant2.isEmpty) unsets += "assRef2"


    $set(sets.toSeq:_*) ++ $unset(unsets:_*)

  }

  def updateClause : MongoDBObject = if(id.isDefined) MongoDBObject("_id" -> id.get) else toMongo
}

object Match{
  def fromMongo(m: MongoDBObject) = {
    val id = m.as[ObjectId]("_id")
    val created = m.as[DateTime]("created")
    val home = m.as[String]("homeTeam")
    val away = m.as[String]("awayTeam")
    val venue = m.as[String]("venue")
    val level = m.as[String]("level")
    val desc = m.getAs[String]("desc")
    val kickoff = m.as[DateTime]("kickoff")
    val refFee = m.getAs[Int]("refFee")
    val assFee = m.getAs[Int]("assFee")
    val intRefs = m.getAsOrElse[List[DBObject]]("intRefs", Nil).map(Referee.fromMongo)
    val intAss = m.getAsOrElse[List[DBObject]]("intAss", Nil).map(Referee.fromMongo)
    val referee = m.getAs[DBObject]("referee").map(Referee.fromMongo)
    val assRef1 = m.getAs[DBObject]("assRef1").map(Referee.fromMongo)
    val assRef2 = m.getAs[DBObject]("assRef2").map(Referee.fromMongo)

    Match(Some(id), created, home, away, venue, level, desc, kickoff, "refereeType", refFee, assFee, intRefs, intAss, referee, assRef1, assRef2)
  }
}

case class Referee(id:ObjectId, name:String, level:String){
  def toMongo = MongoDBObject("_id"->id, "name"->name, "level" -> level)
  def display = "%s (%s)".format(name, level)
}

object Referee{
  def fromUser(u:User) = Referee(u.id.get, u.name, u.level)
  def fromMongo(m: DBObject) = Referee(m.as[ObjectId]("_id"), m.as[String]("name"), m.as[String]("level"))
}

case class User(id:Option[ObjectId], name:String, email:String, telephone:String, level:String, admin:Boolean, created:DateTime, password:String){
  def toMongo  = {
    val builder = MongoDBObject.newBuilder
    if(id.isDefined)
      builder += "_id" -> id.get
    builder += "name" -> name
    builder += "email" -> email
    builder += "tel" -> telephone
    builder += "level" -> level
    builder += "admin" -> admin
    builder += "created" -> created
    builder += "password" -> password
    builder.result()
  }
}

object User{
  def fromMongo(m: DBObject) = {
    val id = m.getAs[ObjectId]("_id")
    val admin = m.getAsOrElse[Boolean]("admin", false)
    val name = m.as[String]("name")
    val email = m.as[String]("email")
    val tel = m.as[String]("tel")
    val level = m.as[String]("level")
    val created = m.as[DateTime]("created")
    val password = m.as[String]("password")
    User(id,name,email,tel,level,admin,created,password)
  }

  def newInstance(name:String, email:String, telephone:String, level:String, password:String) =
    User(None, name, email, telephone, level, false, new DateTime, password)
}

case class Session(userId:ObjectId, username:String, sessionId:String){
  def toMongo : MongoDBObject = {
    MongoDBObject("userid" -> userId, "username" -> username, "sessionid" -> sessionId)
  }
}

object Session{
  def newInstance(userId:ObjectId, username:String) = Session(userId, username, UUID.randomUUID.toString)
  def fromMongo(m:DBObject):Session = Session(m.as[ObjectId]("userId"), m.as[String]("username"), m.as[String]("sessionId"))
}

object Levels{
  val all = List(MenPrem, Men1Div, Men2Div, Men3Div, Men4Div, Men5Div, Men6Div, Men8Div, Boys19, Boys16, Boys15, Boys14, Boys13,
    WomenPrem, Women1Div, Women2Div, Women3Div, Women4Div, Girls19, Girls16, Girls15, Girls14, Girls13)
}

case class SelectOption(key:String, display:String)
case object MenPrem extends SelectOption("menPrem", "Tippeligaen")
case object Men1Div extends SelectOption("men1div", "1. div menn")
case object Men2Div extends SelectOption("men2div", "2. div menn")
case object Men3Div extends SelectOption("men3div", "3. div menn")
case object Men4Div extends SelectOption("men4div", "4. div menn")
case object Men5Div extends SelectOption("men5div", "5. div menn")
case object Men6Div extends SelectOption("men6div", "6. div menn")
case object Men7Div extends SelectOption("men3div", "7. div menn")
case object Men8Div extends SelectOption("men8div", "8. div menn")
case object WomenPrem extends SelectOption("womPrem", "Toppserien")
case object Women1Div extends SelectOption("wom1div", "1. div kvinner")
case object Women2Div extends SelectOption("wom2div", "2. div kvinner")
case object Women3Div extends SelectOption("wom3div", "3. div kvinner")
case object Women4Div extends SelectOption("wom4div", "4. div kvinner")
case object Boys19 extends SelectOption("g19", "Gutter 19")
case object Boys16 extends SelectOption("g16", "Gutter 16")
case object Boys15 extends SelectOption("g15", "Gutter 15")
case object Boys14 extends SelectOption("g14", "Gutter 14")
case object Boys13 extends SelectOption("g13", "Gutter 13")
case object Girls19 extends SelectOption("j19", "Jenter 19")
case object Girls16 extends SelectOption("j16", "Jenter 16")
case object Girls15 extends SelectOption("j15", "Jenter 15")
case object Girls14 extends SelectOption("j14", "Jenter 14")
case object Girls13 extends SelectOption("j13", "Jenter 13")