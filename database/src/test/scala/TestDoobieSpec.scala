package com.eztier.test

import org.scalatest.{BeforeAndAfter, Failed, FunSpec, Matchers}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.eztier.postgres.async._
import com.eztier.postgres.eventstore.models.Patient

import scala.concurrent.Await
import scala.concurrent.duration._

class TestDoobieSpec extends FunSpec with Matchers {
  implicit val system = ActorSystem("Sys")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  // val f = CommandRunner.read2("a")
  val f = CommandRunner.search[Patient]("c")
    .runWith(Sink.seq)

  val r = Await.result(f, 500 millis)

  r.foreach(println(_))

  println("Done")

}
