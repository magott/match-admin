package common

import java.net.URI

import com.mongodb.{ConnectionString, MongoClientOptions, MongoClientSettings, ServerAddress}
import com.mongodb.casbah.{MongoClient, MongoConnection, MongoCredential, MongoDB}
import com.mongodb.client.MongoClients
import com.mongodb.{MongoClient => JavaMongoClient}
import com.mongodb.{MongoClientURI => JavaMongoClientURI}

import scala.util.Properties

object MongoSetting {

  def getMongo: Option[MongoDB]                     = {
    Properties
      .envOrNone("ATLAS_MONGO_URL")
      .map { srvUrl =>
//        val clientSettings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(srvUrl)).retryReads(false).build()
//        val mongoClient    = MongoClients.create(clientSettings)
//        val mongoDatabase  = mongoClient.getDatabase(URI.create(srvUrl).getPath.stripPrefix("/"))
        val javaMongoClient = new JavaMongoClient(new JavaMongoClientURI(srvUrl))
        new MongoClient(javaMongoClient).getDB(URI.create(srvUrl).getPath.stripPrefix("/"))
      }
  }

  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    getMongo.orElse {
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
}
