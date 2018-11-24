package com.eztier.postgres.async

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
// import io.getquill.{PostgresAsyncContext, SnakeCase}

import scala.concurrent.Future
// import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.runtime.universe._
import com.eztier.postgres.eventstore.models._

import doobie._
import doobie.implicits._
import cats.effect.IO

case class MtStreams
(
  typeName: String
)

object CommandRunner {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val cs = IO.contextShift(ec)

  implicit lazy val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:eventstore", "streamer", "streamer"
  )

  // : Future[List[A]] 
  def search[A <: Model](term: String, schema: String = "hl7")(implicit searchable: Searchable[A], typeTag: TypeTag[A]): Source[A, akka.NotUsed] = {
    val t = schema + "." + typeTag.tpe.typeSymbol.name.toString.toLowerCase

    val io = searchable.search(term)

    Source(io.unsafeRunSync())
  }
}
