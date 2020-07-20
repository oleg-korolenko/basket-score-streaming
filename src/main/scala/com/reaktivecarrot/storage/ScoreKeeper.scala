package com.reaktivecarrot.storage

import com.reaktivecarrot.domain.ScoreEvent
import com.reaktivecarrot.exception.ScoreAppException.{ScoreBoxAddException, ScoreBoxRetrievalException}
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidationExceptions

import zio._

object ScoreKeeper {
  type ScoreKeeper = Has[ScoreKeeper.Service]
  type ScoreBox    = Vector[ScoreEvent]

  trait Service {
    def add(event: ScoreEvent): IO[ScoreBoxAddException, ScoreBox]
    def take(numElems: Int): IO[ScoreBoxRetrievalException, ScoreBox]
    def last(): Task[Option[ScoreEvent]]
  }

  val inMemory: ZLayer[Has[ScoreEventValidator.Service] with Has[Ref[ScoreBox]], Nothing, ScoreKeeper] =
    ZLayer.fromServices[ScoreEventValidator.Service, Ref[ScoreBox], Service] { (validator: ScoreEventValidator.Service, scoreBox: Ref[ScoreBox]) =>
      new Service {
        override def add(score: ScoreEvent): IO[ScoreBoxAddException, ScoreBox] = {
          val result: ZIO[Any, Throwable, Serializable] = last().flatMap {
            case Some(lastScore) =>
              val validated: IO[ScoreEventValidationExceptions, ScoreEvent] = validator.validate(score, lastScore)
              val updated: ZIO[Any, ScoreBoxAddException, IO[Nothing, ScoreBox]] = validated.bimap(
                // bubble-up validation exceptions into more generic one
                exceptions => ScoreBoxAddException(score, s"due to validation exceptions $exceptions"),
                // for valid score event we can update or scores and return new scoreBox
                validScore => scoreBox.updateAndGet(scores => scores :+ validScore)
              )
              updated
            // no last score present we can directly push it as 1st one
            case None => scoreBox.updateAndGet(scores => scores :+ score)

          }
          result
        }
        override def take(numElems: Int): IO[ScoreBoxRetrievalException, ScoreBox] =
          scoreBox.get.flatMap { scores =>
            IO.fromEither {
              try { Right(scores.takeRight(numElems)) }
              catch { case _: Throwable => Left(ScoreBoxRetrievalException(numElems)) }
            }
          }
        override def last(): Task[Option[ScoreEvent]] = ???
      }
    }
}
