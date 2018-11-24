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
      // Testing
      val y = xa.yolo
      import y._
      
      val stmt = s"""select * from $schema.patient where name ~* '$term' limit 10
        """

      // https://tpolecat.github.io/doobie-0.2.3/15-FAQ.html
      val q = Query[String, Patient](stmt, None).toQuery0("")      

      q
        .check
        .unsafeRunSync
      /*
      sql"""select * from $schema.patient where name ~* '$term'
        """.query[Patient]
      */

      q
        .stream
        .compile
        .to[List]
        .transact(xa)
    }
  }

}
