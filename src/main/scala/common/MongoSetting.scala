package common

import com.mongodb.ServerAddress
import com.mongodb.casbah.{MongoClient, MongoCredential, MongoConnection, MongoDB}

object MongoSetting {
  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val credential = MongoCredential.createCredential(u, dbName, p.toCharArray)
        val address = new ServerAddress(host, port.toInt)
        val client = MongoClient(address, credential :: Nil)
        Some(client.getDB(dbName))
      case None =>
        Some(MongoConnection("localhost", 27017)("test"))
    }
  }
}
