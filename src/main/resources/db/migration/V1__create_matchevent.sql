--MatchEvent(id:Option[Long], endretAv: String, matchId:String, uuid:String, timestamp: LocalDateTime,
-- description:String, details:String, typ:EventType, level: EventLevel, recipient:Option[String]) {

CREATE TABLE match_event(
  ID SERIAL PRIMARY KEY,
  usr varchar(100),
  matchid varchar(255),
  uuid varchar(255),
  timestamp TIMESTAMP,
  description varchar(500),
  details jsonb,
  eventtype varchar(100),
  eventlevel varchar(100),
  recipient varchar(255) NULL,
  UNIQUE(matchid, uuid, eventtype, recipient)
)