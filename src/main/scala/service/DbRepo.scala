package service

import doobie._
import doobie.implicits._
import cats.effect.IO


/**
  *
  */
object DbRepo {

  case class Person(name:String, age:Int)

  val name:Query0[Person] = sql"select name, age from x".query[Person]
  def params(p:Int) = sql"select name from x where p < $p".query[String]
}

case class DbRepo(tx:Transactor[IO]) {
  def name = DbRepo.name.to[Vector].transact(tx)
}
