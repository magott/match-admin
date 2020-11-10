package common

import java.net.URI

import com.mongodb.casbah.{MongoClient, MongoDB}
import com.mongodb.{MongoClient => JavaMongoClient, MongoClientURI => JavaMongoClientURI}

import scala.util.Properties

object MongoSetting {

  def getMongo: Option[MongoDB]                     = {
    Properties
      .envOrNone("ATLAS_MONGO_URL")
      .map { srvUrl =>
        val javaMongoClient = new JavaMongoClient(new JavaMongoClientURI(srvUrl))
        new MongoClient(javaMongoClient).getDB(URI.create(srvUrl).getPath.stripPrefix("/"))
      }
  }

  def unapply(url: Option[String]): Option[MongoDB] = {
    getMongo.orElse {
      Some(MongoClient("localhost", 27017).getDB("test"))
    }
  }
}
