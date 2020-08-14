package com.reaktivecarrot.storage

import com.reaktivecarrot.{DecodedEventsStream, ScoreBox, ScoreEventsStream, ValidatedEventsStream}
import com.reaktivecarrot.domain.ScoreEvent
import com.reaktivecarrot.exception.{ScoreAppException, ScoreEventValidationException}
import com.reaktivecarrot.exception.ScoreAppException.{ScoreBoxAddException, ScoreBoxRetrievalException}
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidationExceptions
import zio._
import zio.stream.ZStream

object ScoreBox {
  type ScoreBox = Has[ScoreBox.Service]

  trait Service {
    def add(events: ScoreEventsStream): ZStream[Nothing, Any, ScoreEventsStream]
    def take(numElems: Int): ZStream[Nothing, Any, Either[ScoreBoxRetrievalException, ScoreEvent]]
    def last(): Task[Option[ScoreEvent]]
  }

  val inMemory: ZLayer[Has[Ref[Vector[ScoreEvent]]] with ScoreEventValidator.Service, Nothing, ScoreBox] = ???

  /*////              val validated: ValidatedEventsStream = validator.validate(events, lastScore)
            ////              val updated: ZIO[Any, ScoreBoxAddException, IO[Nothing, ScoreBox]] = validated.map{
            ////                (eventOrValidationException: Either[ScoreEventValidationException, ScoreEvent]) =>{
            ////                    eventOrValidationException match {
            ////                      case Right(validEvent)=>{
            ////                        Right(validEvent)
            ////                      }
            ////                      case _ => _
            ////                    }
            ////                }
            ////              }
            //          }
            //
            //
            ////               valid score event we can update or scores and return new scoreBox
            ////                (validScore: Either[ScoreEventValidationException, ScoreEvent]) => {
            ////                  scoreBox.update(scores => scores :+ validScore)
            ////                }
            ////              )
            ////              updated
            ////            // no last score present we can directly push it as 1st one
            ////            case None => ZStream(scoreBox.updateAndGet(scores => scores :+ score)*/

//        override def add(events: DecodedEventsStream): ZStream[Nothing, Any, Either[ScoreBoxAddException, ScoreEvent]] = {
////          val result = last().flatMap {
////            case Some(lastScore) =>
////              val validated: ValidatedEventsStream = validator.validate(events, lastScore)
////              val updated: ZIO[Any, ScoreBoxAddException, IO[Nothing, ScoreBox]] = validated.map{
////                (eventOrValidationException: Either[ScoreEventValidationException, ScoreEvent]) =>{
////                    eventOrValidationException match {
////                      case Right(validEvent)=>{
////                        Right(validEvent)
////                      }
////                      case _ => _
////                    }
////                }
////              }
//          }
//
//
////               valid score event we can update or scores and return new scoreBox
////                (validScore: Either[ScoreEventValidationException, ScoreEvent]) => {
////                  scoreBox.update(scores => scores :+ validScore)
////                }
////              )
////              updated
////            // no last score present we can directly push it as 1st one
////            case None => ZStream(scoreBox.updateAndGet(scores => scores :+ score)
//
//          }
//          result
//        }
//        override def take(numElems: Int): IO[ScoreBoxRetrievalException, ScoreBox] = ???
////          scoreBox.get.flatMap { scores =>
////            IO.fromEither {
////              try { Right(scores.takeRight(numElems)) }
////              catch { case _: Throwable => Left(ScoreBoxRetrievalException(numElems)) }
////            }
////          }
//        override def last(): Task[Option[ScoreEvent]] = ???
//

}
