package data

import org.joda.time.{LocalTime, DateMidnight, DateTime}
import org.bson.types.ObjectId

object MatchValidation {

  def validate(id: Option[String], homeTeam: String, awayTeam: String, venue: String, level: String,
               description: String, kickoffDate: String, kickoffTime: String, refereeType: String, refFee: String,
               assistantFee: String, appointedRef: String, appointedAssistant1: String, appointedAssistant2: String): Either[List[String], Match] = {

    import scalaz._
    import Scalaz.{id => _, _}

    def vHomeTeam = if (homeTeam.isEmpty) "Hjemmelag må være satt".failNel else homeTeam.successNel
    def vAwayTeam = if (awayTeam.isEmpty) "Bortelag må være satt".failNel else awayTeam.successNel
    def vVenue = if (venue.isEmpty) "Bane må være satt".failNel else venue.successNel
    def vRefFee = if (refFee.isEmpty) "Dommerhonorar må være satt".failNel
      else if(refFee.exists(!_.isDigit)) "Dommerhonorar må være heltall (%s)".format(refFee).failNel
      else Some(refFee.toInt).successNel
    def vAssFee = if (assistantFee.isEmpty && refereeType=="trio") "AD-honorar må være satt når det er trio".failNel
      else if(!assistantFee.forall(_.isDigit)) "AD-honorar må være heltall (%s)".format(assistantFee).failNel
      else if(refereeType!="trio") None.successNel
      else Some(assistantFee.toInt).successNel

    def vAppointedRef = None
    def vAppointedAssistant1 = None.successNel
    def vAppointedAssistant2 = None.successNel//TODO Fetch from mongo


    def vRefereeType =
      if (refereeType.isEmpty) "Dommertype må være valgt".failNel
      else if(!List("trio","dommer").contains(refereeType)) "%s er en ugyldig dommertype".format(refereeType).failNel
      else refereeType.successNel

    def vKickoffDate = {
      val vTime = try {
        LocalTime.parse(kickoffTime).successNel
      } catch {
        case _: Exception => (kickoffTime + " er ikke gyldig tidsformat (HH:mm)").failNel
      }
      val vDate = try {
        DateMidnight.parse(kickoffDate).successNel
      } catch {
        case _: Exception => (kickoffDate + " er ikke et gyldig datoformat (yyyy-MM-dd)").failNel
      }

      (vDate |@| vTime){ (date,time) => date.toDateTime.withHourOfDay(time.getHourOfDay).withMinuteOfHour(time.getMinuteOfHour)}
    }

    def nonEmpty(s:String) = if(s.trim.isEmpty) None else Some(s)

    (vKickoffDate |@| vHomeTeam |@| vAwayTeam |@| vVenue |@| vRefereeType |@| vRefFee |@|
      vAssFee) {
      (kickoff, home, away, venue, refType, refFee, assFee) => Match(id.map(new ObjectId(_)), DateTime.now, home, away, venue, level, nonEmpty(description), kickoff, refereeType, refFee, assFee,
        Nil, Nil, None, None, None)
    }.either.left.map(_.list)
  }

  object UserValidation{
    val emailRegex = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""
    def validate(id:Option[ObjectId], name:String, email:String, telephone:String, level:String, refereeNumber:String,password:String, confirmPassword:String) = {
      import org.mindrot.jbcrypt.BCrypt._
      import scalaz._
      import Scalaz.{id => _, _}

      def vPassword = if (password.isEmpty || confirmPassword.isEmpty) "Passord må fylles ut".failNel
        else if(password != confirmPassword) "Passordene er ikke like".failNel
        else if(password.length < 7) "Passord må være på minst 7 tegn".failNel
        else hashpw(password, gensalt()).successNel

      def vName = if(name.isEmpty) "Navn må fylles ut".failNel else name.successNel
      def vEmail = if(email.isEmpty) "E-post må fylles ut".failNel
        else if(!email.matches(emailRegex)) "Ugydlig e-post adresse".failNel
        else email.successNel
      def vTelephone = if(telephone.isEmpty) "Telefon må fylles ut".failNel
        else if(telephone.length < 8 || telephone.exists(!_.isDigit)) "Telefonnummer skal være på 8 siffer og kun bestå av tall".failNel
        else telephone.successNel
      def vRefNumber = if(refereeNumber.isEmpty) "Dommernummer må fylles ut".failNel
        else if(refereeNumber.exists(!_.isDigit)) "Dommernummer skal kun bestå av tall".failNel
        else refereeNumber.toInt.successNel
      def vLevel = if(level.isEmpty) "Nivå må fylles ut".failNel
        else if(Level.asMap.get(level).isEmpty) "Dommernivå er ugyldig".failNel
        else level.successNel

      (vName |@| vEmail |@| vTelephone |@| vLevel |@| vRefNumber |@|vPassword){
        (name, mail, tel, level, refNo, cryptPwd) => User(id, name, mail, tel, level, false, refNo, DateTime.now, cryptPwd)
      }.either.left.map(_.list)
    }
  }
}
