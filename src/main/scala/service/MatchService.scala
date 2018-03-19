package service

import data.{Match, MatchEvent, User}

/**
  *
  */
case class MatchService(repo: MongoRepository) {

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

  }

}