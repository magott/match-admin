package data

import org.joda.time.{LocalTime, DateMidnight, DateTime}
import org.bson.types.ObjectId
import service.MongoRepository

object MatchValidation {

  private val repo = MongoRepository.singletonWithSessionCaching

  def unpublished(homeTeam: String, awayTeam: String, venue: String, level: String,kickoffDate: String,
                  kickoffTime: String, refereeType: String, contactName:String, contactTelephone:String,
                  contactAddress:String, contactZip:String, contactEmail:String) = {
    import scalaz._
    import Scalaz.{id => _, _}
    import BusinessRules._
    def vHomeTeam = if (homeTeam.isEmpty) "Hjemmelag må være satt".failNel else homeTeam.successNel
    def vAwayTeam = if (awayTeam.isEmpty) "Bortelag må være satt".failNel else awayTeam.successNel
    def vVenue = if (venue.isEmpty) "Bane må være satt".failNel else venue.successNel
    def vLevel = if(level.isEmpty) "Nivå for kampen må settes".failNel else if(Level.asMap.get(level).isEmpty) "Ugyldig nivå".failNel else level.successNel
    def vContact = {
      val vName = if(contactName.isEmpty) "Navn på kontakt kan ikke være blankt".failNel else contactName.successNel
      val vAddress = if(contactAddress.isEmpty) "Adresse kan ikke være blank".failNel else contactAddress.successNel
      val vZip = if(contactZip.isEmpty) "Postnummer kan ikke være blankt".failNel
            else if(!contactZip.forall(_.isDigit) || contactZip.length != 4) "Ugyldig postnummer".failNel
            else contactZip.successNel
      val vEmail = validateEmail(contactEmail)
      val vTelephone = validateTelephone(contactTelephone)

      (vName |@| vAddress |@| vZip |@| vTelephone |@| vEmail){ (name, address, zip, telephone, email)
        => ContactInfo(contactName, address, zip, telephone, email)}
    }
    def vRefereeType =
      if (refereeType.isEmpty) "Dommertype må være valgt".failNel
      else if(!RefereeType.asMap.contains(refereeType)) "%s er en ugyldig dommertype".format(refereeType).failNel
      else if(RefereeType.Dommer.key == refereeType && trioOnly(level)) "Vi tillater kun trio for dette nivået (%s)".format(Level.asMap(level)).failNel
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
    (vKickoffDate |@| vHomeTeam |@| vAwayTeam |@| vVenue |@| vRefereeType |@| vLevel |@| vContact){
      (kickoff, home, away, venue, refType, level, contact) => MatchTemplate(home, away, venue, level, kickoff, refereeType, contact)
    }.either.left.map(_.list)

  }

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
    def vAssFee = if (assistantFee.isEmpty && refereeType==RefereeType.Trio.key) "AD-honorar må være satt når det er trio".failNel
      else if(!assistantFee.forall(_.isDigit)) "AD-honorar må være heltall (%s)".format(assistantFee).failNel
      else if(refereeType!="trio") None.successNel
      else Some(assistantFee.toInt).successNel

    def vAppointedRef = if(appointedRef.isEmpty) None.successNel else repo.refereeByUserId(new ObjectId(appointedRef)) match{
        case None => "Finnes ingen dommer registrert med id %s".format(appointedRef).failNel
        case Some(s) => Some(s).successNel
    }

    def vAppointedAssistant1 = if(appointedAssistant1.isEmpty) None.successNel else repo.refereeByUserId(new ObjectId(appointedAssistant1)) match{
      case None => "Finnes ingen dommer registrert med id %s".format(appointedAssistant1).failNel
      case Some(s) => Some(s).successNel
    }

    def vAppointedAssistant2 = if(appointedAssistant2.isEmpty) None.successNel else repo.refereeByUserId(new ObjectId(appointedAssistant2)) match{
      case None => "Finnes ingen dommer registrert med id %s".format(appointedAssistant2).failNel
      case Some(s) => Some(s).successNel
    }

    def vLevel = if(level.isEmpty) "Nivå for kampen må settes".failNel else if(Level.asMap.get(level).isEmpty) "Ugyldig nivå".failNel else level.successNel

    def vPublished = true.successNel

    def vClubContact = if(true) None.successNel else "Ugydlig kontaktinfo for klubbkontakt".failNel

    def vRefereeType =
      if (refereeType.isEmpty) "Dommertype må være valgt".failNel
      else if(!RefereeType.asMap.contains(refereeType)) "%s er en ugyldig dommertype".format(refereeType).failNel
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
      vAssFee |@| vAppointedRef |@| vAppointedAssistant1 |@| vAppointedAssistant2 |@| vLevel |@| vPublished) {
      (kickoff, home, away, venue, refType, refFee, assFee, appointedRef, appointedAss1, appointedAss2, lvl, published) =>
        Match(id.map(new ObjectId(_)), DateTime.now, home, away, venue, lvl, nonEmpty(description), kickoff, refereeType, refFee, assFee,
        Nil, Nil, appointedRef, appointedAss1, appointedAss2, published, None)
    }.either.left.map(_.list)
  }

  object UserValidation{
    val emailRegex = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""
    def validate(id:Option[ObjectId], name:String, email:String, telephone:String, level:String, refereeNumber:String,password:String, confirmPassword:String) = {
      import org.mindrot.jbcrypt.BCrypt._
      import scalaz._
      import Scalaz.{id => _, _}

      def vId = {
        repo.userByEmail(email) match {
          case Some(user) if(user.id != id) => "Det finnes alt en bruker registrert med %s".format(email).failNel
          case _ => id.successNel
        }
      }
      def vPassword = if (password.isEmpty || confirmPassword.isEmpty) "Passord må fylles ut".failNel
        else if(password != confirmPassword) "Passordene er ikke like".failNel
        else if(password.length < 7) "Passord må være på minst 7 tegn".failNel
        else hashpw(password, gensalt()).successNel

      def vName = if(name.isEmpty) "Navn må fylles ut".failNel else name.successNel
      def vEmail = if(email.isEmpty) "E-post må fylles ut".failNel
        else if(!email.matches(emailRegex)) "Ugydlig e-post adresse".failNel
        else email.toLowerCase.successNel
      def vTelephone = if(telephone.isEmpty) "Telefon må fylles ut".failNel
        else if(telephone.length < 8 || telephone.exists(!_.isDigit)) "Telefonnummer skal være på 8 siffer og kun bestå av tall".failNel
        else telephone.successNel
      def vRefNumber = if(refereeNumber.isEmpty) "Dommernummer må fylles ut".failNel
        else if(refereeNumber.exists(!_.isDigit)) "Dommernummer skal kun bestå av tall".failNel
        else refereeNumber.toInt.successNel
      def vLevel = if(level.isEmpty) "Nivå må fylles ut".failNel
        else if(Level.asMap.get(level).isEmpty) "Dommernivå er ugyldig".failNel
        else level.successNel

      (vId |@| vName |@| vEmail |@| vTelephone |@| vLevel |@| vRefNumber |@|vPassword){
        (oid, name, mail, tel, level, refNo, cryptPwd) => User(oid, name, mail, tel, level, false, refNo, DateTime.now, cryptPwd)
      }.either.left.map(_.list)
    }
  }

  object BusinessRules {
    import scalaz._
    import Scalaz.{id => _, _}
    import BusinessRules._
    val emailRegex = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""

    def trioOnly(level:String) = {
      List(Level.MenPrem, Level.Men1Div, Level.Men2Div, Level.Men3Div).exists(_.key == level)
    }

    def validateEmail(email:String) = if(email.isEmpty) "E-post må fylles ut".failNel
      else if(!email.matches(emailRegex)) "Ugydlig e-post adresse".failNel
      else email.toLowerCase.successNel

    def validateTelephone(telephone:String) = if(telephone.isEmpty) "Telefon må fylles ut".failNel
    else if(telephone.length < 8 || telephone.exists(!_.isDigit)) "Telefonnummer skal være på 8 siffer og kun bestå av tall".failNel
    else telephone.successNel
  }

}
