package com.eztier.rest.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString

// akka execution context
import akka.stream.scaladsl.{Flow, Source, Sink}
import akka.stream.{ActorMaterializer, ThrottleMode}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// akka-http-circe
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._


// doobie
import com.eztier.postgres.eventstore.models.{Patient, CaPatient, Model}
import com.eztier.postgres.async.CommandRunner

// For testing
case class Dummy(name: String)

trait SearchStreamRoutes {
  implicit val actorSystem: ActorSystem
  implicit val streamMaterializer: ActorMaterializer
  implicit val executionContext: ExecutionContext

  lazy val httpStreamingRoutes = streamingJsonRoute
  lazy val httpInfoStreamingRoutes = streamingInfoRoute
  lazy val httpStreamingSearchRoutes = streamingSearchRoute

  implicit val jsonStreamingSupport: akka.http.scaladsl.common.JsonEntityStreamingSupport = EntityStreamingSupport.json()
  
  def streamingInfoRoute =
    path("info") {
      get {
        val sourceOfNumbers = Source(1 to 15)
        val byteStringSource =
          sourceOfNumbers.map(num => s"mrn:$num")
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)
            .map(_.toString)
            .map(s => ByteString(s))

        complete(HttpEntity(`text/plain(UTF-8)`, byteStringSource))
      }
    }

  /*
    @test
      for i in {1..10000}; do curl localhost:9000/streaming-json & done
  */
  def streamingJsonRoute =
    path("streaming-json") {
      get {
        val sourceOfNumbers = Source(1 to 15)
        val sourceOfSearchMessages =
          sourceOfNumbers.map(num => Patient(s"name:$num"))
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)

        complete(sourceOfSearchMessages)
      }
    }
    
  /*
    @test
      curl -XPOST -H 'Content-Type:application/json'  -d '{"name": "abc"}' localhost:9000/search
  */
  def streamingSearchRoute = {
    path("search") {
      get {
        val sourceOfNumbers = Source(1 to 15)
        val sourceOfSearchMessages =
          sourceOfNumbers.map(num => Dummy(s"name:$num"))
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)

        complete(sourceOfSearchMessages)
      } ~ post {
        entity(as[Patient]) { p =>
          val resp = CommandRunner
            .search[CaPatient](p.name)
            .throttle(elements = 100, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)
            
          complete(resp)
        }
      }
    }

  }

}
