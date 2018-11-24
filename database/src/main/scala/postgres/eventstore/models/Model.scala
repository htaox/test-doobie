package com.eztier.postgres.eventstore.models

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import doobie._
import doobie.implicits._
import cats.effect.IO
import scala.concurrent.Future

trait Model {
  def name: String
}

trait Searchable[A <: Model] {
  def search(term: String, schema: String = "hl7")(implicit xa: Transactor[IO]): IO[List[A]]
}

object Searchable {
  implicit object PatientSearch extends Searchable[Patient] {
    override def search(term: String, schema: String = "hl7")(implicit xa: Transactor[IO]): IO[List[Patient]] = {
      
      // https://tpolecat.github.io/doobie/docs/17-FAQ.html#how-do-i-turn-an-arbitrary-sql-string-into-a-query0update0
      val stmt = fr"""select * from """ ++ Fragment(schema, None) ++ fr""".patient where name ~* """ ++ Fragment(s"'$term'", None) ++ fr""" limit 10"""

      // Testing
      val y = xa.yolo
      import y._
      
      stmt
        .query[Patient]
        .check
        .unsafeRunSync
      // Fin Testing
      
      stmt
        .query[Patient]
        .stream
        .compile
        .to[List]
        .transact(xa)
    }
  }

}
