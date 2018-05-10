package service

import data.{Match, MatchEvent, User}
import org.bson.types.ObjectId

/**
  *
  */
case class MatchService(repo: MongoRepository, dbRepo: DbRepo, sendRegning: Option[SendRegning]) {

  def deleteMatch(matchId:String) = {
    repo.deleteMatch(new ObjectId(matchId))
    dbRepo.deleteEvents(matchId)
  }

  def saveMatch(m: Match, user:User) = {
    m.id.flatMap(id => {
      repo.fullMatch(id).flatMap(old => {
        if(!old.published) Some(MatchEvent.kampPublisert(m, user))
        else if(m.refString != old.refString) Some(MatchEvent.oppsattDommer(m, user))
        else None
      })
    }).foreach(saveEvent)
    repo.saveMatch(m)
  }

  def saveEvent(event: MatchEvent): Unit ={
    println(s"Et event skjedde $event")
    dbRepo.insert(event)
  }

  def opprettRegning(m: Match, user:User) = {
    val resultat = sendRegning.get.opprettRegningPaKamp(m)
    resultat.map(regningsnr => MatchEvent.opprettetRegning(m, user, regningsnr))
      .foreach(dbRepo.insert)
    resultat.map(regningsnr => repo.saveMatch(m.copy(invoice = Some(regningsnr))))
    resultat
  }

}
