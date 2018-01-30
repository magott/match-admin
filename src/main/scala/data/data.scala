package data

import org.joda.time.{DateTime, LocalDateTime}
import org.bson.types.ObjectId
import com.mongodb.casbah.query.Imports._
import java.util.{Collections, UUID, List => JList}

import io.circe.Json

import collection.mutable
import collection.mutable.ArrayBuffer
import xml.NodeSeq
import scala.collection.JavaConverters._

case class Match(id:Option[ObjectId], created:DateTime, homeTeam:String, awayTeam:String, venue:String, level:String,
                 description:Option[String], kickoff:DateTime, refereeType:String, refFee:Option[Int],
                 assistantFee:Option[Int], interestedRefs:List[Referee], interestedAssistants:List[Referee],
                 appointedRef:Option[Referee], appointedAssistant1:Option[Referee], appointedAssistant2: Option[Referee],
                 published:Boolean, adminOk:Boolean ,clubContact:Option[ContactInfo], payerEmail:String, payingTeam: Option[String]){

  def idString = id.map(_.toString).getOrElse("")
  def kickoffDateTimeString = kickoff.toString("dd.MM.yyyy HH:mm")
  def teams:String = "%s - %s".format(homeTeam, awayTeam)
  def isInterestedRef(userId: String) : Boolean = interestedRefs.find(_.id.toString == userId).isDefined
  def isInterestedAssistant(userId: String) : Boolean = interestedAssistants.find(_.id.toString == userId).isDefined
  def areAssistantsAppointed = appointedAssistant1.isDefined && appointedAssistant2.isDefined
  def showInterestedRefIcon = appointedRef.isEmpty && interstedRefsAvailable(interestedRefs)
  def showAss1RefIcon = appointedAssistant1.isEmpty && interstedRefsAvailable(interestedAssistants)
  def showAss2RefIcon = appointedAssistant2.isEmpty && interstedRefsAvailable(interestedAssistants)
  private def interstedRefsAvailable(interested:List[Referee]) = !interested.filterNot(Set(appointedAssistant1, appointedAssistant2, appointedRef).flatten.contains(_)).isEmpty

//  def updateClause : MongoDBObject = id.map(_id => MongoDBObject("_id" -> _id)).getOrElse(MongoDBObject.empty)
  def updateClause : MongoDBObject =  MongoDBObject("_id" -> id.get)

  def isAppointed(userId:String) = appointedRef.exists(_.id.toString == userId)

  def assigned = if(refereeType == RefereeType.Trio.key) (List(appointedRef,appointedAssistant1,appointedAssistant2).forall(_.isDefined)) else appointedRef.isDefined

  private def setsAndUnsets = {
    val unsets = mutable.MutableList.empty[String]

    val sets = mutable.Map[String, Any](
      "created" -> created,
      "homeTeam" -> homeTeam,
      "awayTeam" -> awayTeam,
      "venue" -> venue,
      "level" -> level,
      "kickoff" -> kickoff,
      "refereeType" -> refereeType,
      "published" -> true, //Matches always saved as published, unpublished -> MatchTemplate
      "payerEmail" -> payerEmail
    )
    payingTeam.foreach(x => sets += "payingTeam" -> x)
    description.foreach(x => sets += "desc" -> x)
    refFee.foreach(x => sets += "refFee" -> x)
    assistantFee.foreach(x => sets += "assFee" -> x)
    appointedRef.foreach(x => sets += "referee" -> x.toMongo)
    appointedAssistant1.foreach(x => sets += "assRef1" -> x.toMongo)
    appointedAssistant2.foreach(x => sets += "assRef2" -> x.toMongo)
    clubContact.foreach(x => sets += "clubContact" -> x.toMongo)

    if(description.isEmpty) unsets += "desc"
    if(refFee.isEmpty) unsets += "refFee"
    if(assistantFee.isEmpty) unsets += "assFee"
    if(appointedRef.isEmpty) unsets += "referee"
    if(appointedAssistant1.isEmpty) unsets += "assRef1"
    if(appointedAssistant2.isEmpty) unsets += "assRef2"
    if(clubContact.isEmpty) unsets += "clubContact" //TODO: Does it work?
    (sets, unsets)
  }
  
  def asUpdate: MongoDBObject  = {
    val (sets, unsets) = setsAndUnsets
    val mongoDBObject = $set(sets.toSeq:_*) ++ $unset(unsets:_*)
    mongoDBObject
  }

  def asInsert: MongoDBObject = {
    val sets = setsAndUnsets._1
    MongoDBObject(sets.toSeq:_*)
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
      <div>{appointedAssistant2.map(_.name).getOrElse("")}</div>
    }
  }

  def availableCount:Int = {
    appointedRef.map(_=>0).getOrElse(1) +
      appointedAssistant1.map(_ => 0).getOrElse(if(refereeType==RefereeType.Trio.key) 1 else 0) +
      appointedAssistant2.map(_ => 0).getOrElse(if(refereeType==RefereeType.Trio.key) 1 else 0)
  }


  def buttonTexts = (
    <span class="int interested-txt"> Interesse meldt</span>
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

  def adminButton = (
    <button type="button" class="btn" id="admin-status" data-state={if(adminOk)"done" else "open"}>
      <span class="open-text"><i class="icon-star-empty"></i> Åpen</span>
      <span class="done-text"><i class="icon-ok"></i> Ferdig</span>
    </button>
  )

  def betalendeLag = {
    payingTeam.flatMap(Paying.fromString) match {
      case Some(Home) => homeTeam
      case Some(Away) => awayTeam
      case None => "Ikke oppgitt i bestilling, hør med lagene på kampdag"
    }
  }

  def dommerregningSendesTil = {
    if (payerEmail.isEmpty)
      clubContact.map(_.email).filterNot(_.isEmpty).getOrElse("Ingen epost oppgitt i bestilling, hør med lagene på kampdag")
    else payerEmail

  }

  def asJson: Json ={
    import io.circe.literal._
    import io.circe._, io.circe.syntax._
    json"""{
          "kamp":$teams,
          "bane":$venue,
          "avspark":$kickoffDateTimeString,
          "dommer":${appointedRef.map(_.name)},
          "AD1":${appointedAssistant1.map(_.name)},
          "AD2":${appointedAssistant2.map(_.name)},
          }"""
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
    val published = m.getAsOrElse[Boolean]("published", true);
    val adminOk = m.getAsOrElse[Boolean]("adminOk", false);
//    val intRefs = m.getAsOrElse[ArrayBuffer[DBObject]]("intRef", ArrayBuffer.empty).map(Referee.fromMongo).toList
    val intRefs:List[Referee] = m.getAs[List[DBObject]]("intRef").map(_.map(Referee.fromMongo)).getOrElse(List.empty)
    val intAss:List[Referee] = m.getAs[List[DBObject]]("intAss").map(_.map(Referee.fromMongo)).getOrElse(List.empty)
    val referee = m.getAs[DBObject]("referee").map(Referee.fromMongo)
    val assRef1 = m.getAs[DBObject]("assRef1").map(Referee.fromMongo)
    val assRef2 = m.getAs[DBObject]("assRef2").map(Referee.fromMongo)
    val clubContact = m.getAs[DBObject]("clubContact").map(ContactInfo.fromMongo)
    val payerEmail = m.getAsOrElse[String]("payerEmail","")
    val payingTeam = m.getAs[String]("payingTeam")


    Match(Some(id), created, home, away, venue, level, desc, kickoff, refereeType, refFee, assFee, intRefs, intAss, referee, assRef1, assRef2, published, adminOk,clubContact, payerEmail, payingTeam)
  }

  def newInstance(homeTeam:String, awayTeam:String, venue:String, level:String,
                  description:Option[String], kickoff:DateTime, refereeType:String, refFee:Option[Int],
                  assistantFee:Option[Int], interestedRefs:List[Referee], interestedAssistants:List[Referee],
                  appointedRef:Option[Referee], appointedAssistant1:Option[Referee], appointedAssistant2: Option[Referee]
                  ) =
    Match(None, DateTime.now, homeTeam, awayTeam, venue, level, description, kickoff, refereeType, refFee, assistantFee, Nil, Nil, appointedRef, appointedAssistant1, appointedAssistant2, true, false, None, "", None)


}
case class MatchTemplate(homeTeam: String, awayTeam: String, venue: String, level: String,kickoff:DateTime,
                         refereeType: String, clubContact:ContactInfo, payerEmail:String, payingTeam: String){

  def dateTimeString = kickoff.toString("dd.MM.yyyy HH:mm")
  def teams = "%s - %s".format(homeTeam,awayTeam)
  def toMongo:MongoDBObject = MongoDBObject("homeTeam" -> homeTeam, "awayTeam" -> awayTeam, "venue" -> venue, "level" -> level,
                              "kickoff" -> kickoff, "published" -> false,"refereeType" -> refereeType,
                               "created" -> DateTime.now, "clubContact" -> clubContact.toMongo,
                                "payerEmail" -> payerEmail, "payingTeam" -> payingTeam)
  def betalendeLag = Paying.fromString(payingTeam) match {
    case Some(Home) => homeTeam
    case Some(Away) => awayTeam
    case _ => "Ukjent"
  }
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

case class User(id:Option[ObjectId], name:String, email:String, telephone:String, level:String, admin:Boolean, refereeNumber:Int, created:DateTime, password:String, lastUpdate:Option[DateTime] = None){
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
    builder += "updated" -> DateTime.now()
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
    val updated = m.getAs[DateTime]("updated")
    User(id,name,email,tel,level,admin,refNo,created,password, updated)
  }

  def newInstance(name:String, email:String, telephone:String, level:String, refereeNumber:Int, password:String) =
    User(None, name, email, telephone, level, false, refereeNumber, new DateTime, password)

  def toIdNameJson(u:User) = s"""{"id":"${u.id.getOrElse("")}", "label":"${u.name}"}"""

  def system = User(None, "System","code@andersen-gott.com","","",true,0,DateTime.now,"",None)
}

case class ContactInfo(name:String, address:String, zip:String, telephone:String, email:String){
  def toMongo : MongoDBObject = MongoDBObject("name" -> name, "address" -> address, "zip" -> zip, "telephone" -> telephone, "email" -> email)
}
object ContactInfo{
  def fromMongo(m:DBObject) = ContactInfo(m.as[String]("name"), m.as[String]("address"), m.as[String]("zip"), m.as[String]("telephone"), m.as[String]("email"))
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

  object MenPrem extends KeyAndValue("menPrem", "Tippeligaen")
  object Men1Div extends KeyAndValue("men1div", "1. div menn")
  object Men2Div extends KeyAndValue("men2div", "2. div menn")
  object Men3Div extends KeyAndValue("men3div", "3. div menn")
  object Men4Div extends KeyAndValue("men4div", "4. div menn")
  object Men5Div extends KeyAndValue("men5div", "5. div menn")
  object Men6Div extends KeyAndValue("men6div", "6. div menn")
  object Men7Div extends KeyAndValue("men7div", "7. div menn")
  object Men8Div extends KeyAndValue("men8div", "8. div menn")
  object WomenPrem extends KeyAndValue("womPrem", "Toppserien")
  object Women1Div extends KeyAndValue("wom1div", "1. div kvinner")
  object Women2Div extends KeyAndValue("wom2div", "2. div kvinner")
  object Women3Div extends KeyAndValue("wom3div", "3. div kvinner")
  object Women4Div extends KeyAndValue("wom4div", "4. div kvinner")
  object Boys19IK extends KeyAndValue("g19ik", "Gutter 19 Interkrets")
  object Boys19 extends KeyAndValue("g19", "Gutter 19")
  object Boys16IK extends KeyAndValue("g16ik", "Gutter 16 Interkrets")
  object Boys16 extends KeyAndValue("g16", "Gutter 16")
  object Boys15 extends KeyAndValue("g15", "Gutter 15")
  object Boys14 extends KeyAndValue("g14", "Gutter 14")
  object Boys13 extends KeyAndValue("g13", "Gutter 13")
  object Girls19 extends KeyAndValue("j19", "Jenter 19")
  object Girls16 extends KeyAndValue("j16", "Jenter 16")
  object Girls15 extends KeyAndValue("j15", "Jenter 15")
  object Girls14 extends KeyAndValue("j14", "Jenter 14")
  object Girls13 extends KeyAndValue("j13", "Jenter 13")

  val all = List(MenPrem, Men1Div, Men2Div, Men3Div, Men4Div, Men5Div, Men6Div, Men7Div, Men8Div, Boys19IK, Boys19, Boys16IK, Boys16, Boys15, Boys14, Boys13,
    WomenPrem, Women1Div, Women2Div, Women3Div, Women4Div, Girls19, Girls16, Girls15, Girls14, Girls13)
  val asMap: Map[String, String] = all.foldLeft(Map.empty[String,String])((acc, opt) => acc.+((opt.key, opt.display)))
}

sealed abstract class Paying(team:String)
case object Home extends Paying("home")
case object Away extends Paying("away")

object Paying{
  def fromString(s:String): Option[Paying] = {
    s match {
      case "home" => Some(Home)
      case "away" => Some(Away)
      case _ => None
    }
  }
  def isValid(s:String) = fromString(s).isDefined
}

object PayingTeam{
  object Home extends KeyAndValue("home", "Hjemmelag")
  object Away extends KeyAndValue("away", "Bortelag")
  val all = List(Home, Away)
  def isValid(value:String) = all.find(_.key == value).isDefined
  def fromString(value: String) = all.find(_.key == value)
}

object RefereeType{
  object Dommer extends KeyAndValue("dommer", "Dommer")
  object Trio extends KeyAndValue("trio", "Trio")
  val all = List(Dommer, Trio)
  val asMap = Map(Dommer.key -> Dommer.display, Trio.key -> Trio.display)
  def displayName(key: String) = asMap.get(key).get
}

case class KeyAndValue(key:String, display:String)