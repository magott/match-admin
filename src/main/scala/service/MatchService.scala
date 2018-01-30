package service

import data.{Match, MatchEvent}

/**
  *
  */
class MatchService(repo: MongoRepository) {

  def saveMatch(m: Match) = {
    m.id.flatMap(id => {
      repo.fullMatch(id).map(m => {
        if(!m.published) MatchEvent.kampPublisert(m)
        else{
          MatchEvent.oppsattDommer(m)
        }
      })
    })
    repo.saveMatch(m)
  }

}
