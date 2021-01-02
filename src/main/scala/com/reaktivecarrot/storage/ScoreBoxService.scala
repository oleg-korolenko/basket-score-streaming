package com.reaktivecarrot.storage

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception._
import com.reaktivecarrot.exception.ScoreAppException._
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator._
import zio._
import zio.stream.ZStream
import zio.console.putStr

object ScoreBoxService {
  type ScoreBoxService = Has[Service]

  trait Service {
    def add[R](events: ScoreEventsStream[R]): ScoreEventsStream[R]
    def take(numElems: Int): ZStream[Any, Nothing, ScoreEvent]
    def last(): ZStream[Any, Nothing, Option[ScoreEvent]]
  }

  val inMemory: ZLayer[Has[Ref[ScoreBox]], Nothing, ScoreBoxService] =
    ZLayer.fromService[Ref[ScoreBox], Service] { (scoreBox: Ref[ScoreBox]) =>
      new Service {
        override def add[R](events: ScoreEventsStream[R]): ScoreEventsStream[R] = {
          events
            .map {
              case Right(event) =>
                println(s"event to add $event")
                // scoreBox.updateAndGet(box => box.copy(events = box.events :+ event, lastEvent = Some(event)))
                scoreBox.update(box => ScoreBox(events = Vector(event), lastEvent = Some(event)))
                Right(event)
              case Left(exception) => Left(exception)
            }
        }

        override def take(numElems: Int): ZStream[Any, Nothing, ScoreEvent] = {
          ZStream.fromIterableM {
            scoreBox.get.map { _.events.takeRight(numElems) }
          }
        }

        override def last(): ZStream[Any, Nothing, Option[ScoreEvent]] =
          ZStream.fromEffect {
            scoreBox.get.map { _.lastEvent }
          }
      }
    }

  def add[R](events: ScoreEventsStream[R]): ZStream[R with ScoreBoxService, Nothing, ScoreEventOr[ScoreAppException]] =
    ZStream.accessStream[R with ScoreBoxService](_.get.add(events))

  def take(numElems: Int): ZStream[ScoreBoxService, Nothing, ScoreEvent] =
    ZStream.accessStream[ScoreBoxService](_.get.take(numElems))

  def last(): ZStream[ScoreBoxService, Nothing, Option[ScoreEvent]] =
    ZStream.accessStream[ScoreBoxService](_.get.last())
}
