package common

import com.mongodb.{MongoClientOptions, ServerAddress}
import com.mongodb.casbah.{MongoClient, MongoConnection, MongoCredential, MongoDB}

object MongoSetting {
  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val mongoClientOptions = MongoClientOptions.builder().retryWrites(false).build()
        val credential         = MongoCredential.createCredential(u, dbName, p.toCharArray)
        val address            = new ServerAddress(host, port.toInt)
        val client             = MongoClient(address, credential :: Nil, mongoClientOptions)
        Some(client.getDB(dbName))
      case None                                  =>
        Some(MongoClient("localhost", 27017).getDB("test"))
    }
  }
}
