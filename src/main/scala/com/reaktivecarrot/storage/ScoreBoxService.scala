package com.reaktivecarrot.storage

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception._
import zio._
import zio.stream.ZStream

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
            .mapM {
              case Right(event) =>
                scoreBox.updateAndGet(box => box.copy(events = box.events :+ event, lastEvent = Some(event))) *> ZIO.right(event)
              case Left(exception) => ZIO.left(exception)
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

  def add[R](events: ScoreEventsStream[R]): ZStream[R with ScoreBoxService, Throwable, ScoreEventOr[ScoreAppException]] =
    ZStream.accessStream[R with ScoreBoxService](_.get.add(events))

  def take(numElems: Int): ZStream[ScoreBoxService, Nothing, ScoreEvent] =
    ZStream.accessStream[ScoreBoxService](_.get.take(numElems))

  def last(): ZStream[ScoreBoxService, Nothing, Option[ScoreEvent]] =
    ZStream.accessStream[ScoreBoxService](_.get.last())
}
