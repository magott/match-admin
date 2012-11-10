package data

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.mongodb.casbah.query.Imports._
import java.util.UUID
import collection.mutable
import collection.mutable.ArrayBuffer
import xml.NodeSeq

case class Match(id:Option[ObjectId], created:DateTime, homeTeam:String, awayTeam:String, venue:String, level:String,
                 description:Option[String], kickoff:DateTime, refereeType:String, refFee:Option[Int],
                 assistantFee:Option[Int], interestedRefs:List[Referee], interestedAssistants:List[Referee],
                 appointedRef:Option[Referee], appointedAssistant1:Option[Referee], appointedAssistant2: Option[Referee]){

  def teams:String = "%s - %s".format(homeTeam, awayTeam)
  def isInterestedRef(userId: String) : Boolean = interestedRefs.find(_.id.toString == userId).isDefined
  def isInterestedAssistant(userId: String) : Boolean = interestedAssistants.find(_.id.toString == userId).isDefined
  def areAssistantsAppointed = appointedAssistant1.isDefined && appointedAssistant2.isDefined

  def updateClause : MongoDBObject = if(id.isDefined) MongoDBObject("_id" -> id.get) else toMongo

  def toMongo: MongoDBObject  = {
    val unsets = mutable.MutableList.empty[String]

    val sets = mutable.Map[String, Any](
      "created" -> created,
      "homeTeam" -> homeTeam,
      "awayTeam" -> awayTeam,
      "venue" -> venue,
      "level" -> level,
      "kickoff" -> kickoff,
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
  def interestedRefButton(userId:Option[String]) =
    if(appointedRef.isEmpty)
      interestedButton(userId, "ref", isInterestedRef)
    else
      <div>{appointedRef.get.name}</div>


  def interestedAssistant1Button(userId:Option[String]) = {
    if(appointedAssistant1.isEmpty){
      interestedButton(userId, "assRef", isInterestedAssistant)
    }else{
      <div>{appointedAssistant1.get.name}</div>
    }
  }

  def interestedAssistant2Button(userId:Option[String]):NodeSeq = {
    if(appointedAssistant1.isDefined && appointedAssistant2.isEmpty){
      interestedButton(userId, "assRef", isInterestedAssistant)
    }else{
      <div>{appointedAssistant1.map(_.name).getOrElse("")}</div>
    }
  }

  def buttonTexts = (
    <span class="int interested-txt"><i class="icon-ok icon-white"/> Interesse meldt</span>
      <span class="int not-interested-txt">Meld interesse</span>
      <span class="int hover-interested-txt">Meld av</span>
      <span class="int assigned-txt">Kampen er tildelt</span>
      <span class="int login-txt">Logg inn for å melde interesse</span>
    )

  def interestedButton(userId:Option[String], buttonId:String, isInterested: String => Boolean) = {
      if(userId.isEmpty)
        <a href="/login" class="btn">Logg inn for å melde interesse</a>
      else if(isInterested(userId.get))
        <button id={buttonId} class="btn" data-state="interested">{buttonTexts}</button>
      else
        <button id={buttonId} class="btn int" data-state="not-interested">{buttonTexts}</button>
  }

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
    val refereeType = m.as[String]("refereeType")
    val refFee = m.getAs[Int]("refFee")
    val assFee = m.getAs[Int]("assFee")
    val intRefs = m.getAsOrElse[ArrayBuffer[DBObject]]("intRef", ArrayBuffer.empty).map(Referee.fromMongo).toList
    val intAss = m.getAsOrElse[ArrayBuffer[DBObject]]("intAss", ArrayBuffer.empty).map(Referee.fromMongo).toList
    val referee = m.getAs[DBObject]("referee").map(Referee.fromMongo)
    val assRef1 = m.getAs[DBObject]("assRef1").map(Referee.fromMongo)
    val assRef2 = m.getAs[DBObject]("assRef2").map(Referee.fromMongo)

    Match(Some(id), created, home, away, venue, level, desc, kickoff, refereeType, refFee, assFee, intRefs, intAss, referee, assRef1, assRef2)
  }

  def newInstance(homeTeam:String, awayTeam:String, venue:String, level:String,
                  description:Option[String], kickoff:DateTime, refereeType:String, refFee:Option[Int],
                  assistantFee:Option[Int], interestedRefs:List[Referee], interestedAssistants:List[Referee],
                  appointedRef:Option[Referee], appointedAssistant1:Option[Referee], appointedAssistant2: Option[Referee]) =
    Match(None, DateTime.now, homeTeam, awayTeam, venue, level, description, kickoff, refereeType, refFee, assistantFee, Nil, Nil, appointedRef, appointedAssistant1, appointedAssistant2)

}

case class Referee(id:ObjectId, name:String, level:String){
  def toMongo = MongoDBObject("_id"->id, "name"->name, "level" -> level)
  def display = "%s (%s)".format(name, Level.asMap(level))
  def toSelectOption = KeyAndValue(id.toString,display)
}

object Referee{
  def fromUser(u:User) = Referee(u.id.get, u.name, u.level)
  def fromMongo(m: DBObject) = Referee(m.as[ObjectId]("_id"), m.as[String]("name"), m.as[String]("level"))
}

case class User(id:Option[ObjectId], name:String, email:String, telephone:String, level:String, admin:Boolean, refereeNumber:Int, created:DateTime, password:String){
  def toMongo  = {
    val builder = MongoDBObject.newBuilder
    if(id.isDefined)
      builder += "_id" -> id.get

    builder += "admin" -> admin
    builder += "name" -> name
    builder += "email" -> email
    builder += "tel" -> telephone
    builder += "level" -> level

    builder += "refNo" -> refereeNumber
    builder += "created" -> created
    builder += "password" -> password
    builder.result()
  }
  def updateClause : MongoDBObject = if(id.isDefined) MongoDBObject("_id" -> id.get) else toMongo
}

object User{
  def fromMongo(m: DBObject) = {
    val id = m.getAs[ObjectId]("_id")
    val admin = m.getAsOrElse[Boolean]("admin", false)
    val name = m.as[String]("name")
    val email = m.as[String]("email")
    val tel = m.as[String]("tel")
    val level = m.as[String]("level")
    val refNo = m.as[Int]("refNo")
    val created = m.as[DateTime]("created")
    val password = m.as[String]("password")
    User(id,name,email,tel,level,admin,refNo,created,password)
  }

  def newInstance(name:String, email:String, telephone:String, level:String, refereeNumber:Int, password:String) =
    User(None, name, email, telephone, level, false, refereeNumber, new DateTime, password)
}

case class Session(userId:ObjectId, username:String, name:String, admin:Boolean, sessionId:String, expires:DateTime){
  def toMongo : MongoDBObject = {
    MongoDBObject("userId" -> userId, "username" -> username, "name" -> name, "admin"->admin, "sessionId" -> sessionId, "expires" -> expires)
  }
}

object Session{
  def fromUser(u:User, sessionId:String):Session = fromUser(u, sessionId, DateTime.now.plusDays(1))
  def fromUser(u:User, sessionId:String, expires:DateTime):Session = Session(u.id.get, u.email, u.name, u.admin, sessionId,expires)
  def newInstance(u:User, expires:DateTime):Session = fromUser(u, UUID.randomUUID.toString, expires)
  def newInstance(u:User):Session = newInstance(u, DateTime.now.plusDays(1))
  def fromMongo(m:DBObject):Session = Session(m.as[ObjectId]("userId"), m.as[String]("username"), m.as[String]("name"), m.getAsOrElse[Boolean]("admin",false), m.as[String]("sessionId"), m.getAsOrElse[DateTime]("expires", DateTime.now))
}

object Level{

  case object MenPrem extends KeyAndValue("menPrem", "Tippeligaen")
  case object Men1Div extends KeyAndValue("men1div", "1. div menn")
  case object Men2Div extends KeyAndValue("men2div", "2. div menn")
  case object Men3Div extends KeyAndValue("men3div", "3. div menn")
  case object Men4Div extends KeyAndValue("men4div", "4. div menn")
  case object Men5Div extends KeyAndValue("men5div", "5. div menn")
  case object Men6Div extends KeyAndValue("men6div", "6. div menn")
  case object Men7Div extends KeyAndValue("men3div", "7. div menn")
  case object Men8Div extends KeyAndValue("men8div", "8. div menn")
  case object WomenPrem extends KeyAndValue("womPrem", "Toppserien")
  case object Women1Div extends KeyAndValue("wom1div", "1. div kvinner")
  case object Women2Div extends KeyAndValue("wom2div", "2. div kvinner")
  case object Women3Div extends KeyAndValue("wom3div", "3. div kvinner")
  case object Women4Div extends KeyAndValue("wom4div", "4. div kvinner")
  case object Boys19 extends KeyAndValue("g19", "Gutter 19")
  case object Boys16 extends KeyAndValue("g16", "Gutter 16")
  case object Boys15 extends KeyAndValue("g15", "Gutter 15")
  case object Boys14 extends KeyAndValue("g14", "Gutter 14")
  case object Boys13 extends KeyAndValue("g13", "Gutter 13")
  case object Girls19 extends KeyAndValue("j19", "Jenter 19")
  case object Girls16 extends KeyAndValue("j16", "Jenter 16")
  case object Girls15 extends KeyAndValue("j15", "Jenter 15")
  case object Girls14 extends KeyAndValue("j14", "Jenter 14")
  case object Girls13 extends KeyAndValue("j13", "Jenter 13")

  val all = List(MenPrem, Men1Div, Men2Div, Men3Div, Men4Div, Men5Div, Men6Div, Men8Div, Boys19, Boys16, Boys15, Boys14, Boys13,
    WomenPrem, Women1Div, Women2Div, Women3Div, Women4Div, Girls19, Girls16, Girls15, Girls14, Girls13)
  val asMap = all.foldLeft(Map.empty[String,String])((acc, opt) => acc.+((opt.key, opt.display)))
}

object RefereeType{
  case object Dommer extends KeyAndValue("dommer", "Dommer")
  case object Trio extends KeyAndValue("trio", "Trio")
  val all = List(Dommer, Trio)
  val asMap = Map(Dommer.key -> Dommer.display, Trio.key -> Trio.display)
}

case class KeyAndValue(key:String, display:String)