package com.reaktivecarrot.validation

import com.reaktivecarrot.{DecodedEventsStream, ScoreBox, ScoreEventsStream, ValidatedEventsStream}
import com.reaktivecarrot.domain.{ScoreEvent, Team1, Team2}
import com.reaktivecarrot.exception.ScoreAppException.{ScoreEventMatchTimeValidationException, ScoreEventTeamScoreIncreaseValidationException, ScoreEventTeamTotalsValidationException}
import com.reaktivecarrot.exception.ScoreEventValidationException
import zio._
import zio.stream.ZStream

object ScoreEventValidator {

  type ScoreEventValidator            = Has[Service]
  type ScoreEventValidationExceptions = Seq[ScoreEventValidationException]

  trait Service {
    def validate(event: DecodedEventsStream, prev: ScoreEvent): ValidatedEventsStream
  }

  val live: ZLayer[Has[ScoreBox], Nothing, ScoreEventValidator] = ZLayer.fromService[ScoreBox, Service] { (scoreBox: ScoreBox) =>
    new Service {
//
      // could have used cats.Validated or similar
      override def validate(events: ScoreEventsStream): ZStream[Any, Nothing, Either[ScoreEventValidationExceptions, ScoreEvent]] = {

//        val result = maybePrev match{
//              case None => IO.succeed(event)
//              case Some(prev) =>
//                val validationExceptions =
//                  checkMatchTimeIsNotSmaller(event, prev) ++ checkTotalsIsNotSmaller(event, prev) ++ validatePointsIncreaseAndTotals(event, prev)
//
//                validationExceptions match {
//                  case Seq() => IO.succeed(event)
//                  case _     => IO.fail(validationExceptions)
//                }
//
//            }
//        }
//        result
//      }

//      private[this] def checkMatchTimeIsNotSmaller(event: ScoreEvent, prev: ScoreEvent) = {
//        if (event.matchTime.secs < prev.matchTime.secs) Seq(ScoreEventMatchTimeValidationException(event.matchTime, prev.matchTime))
//        else Seq.empty[ScoreEventValidationException]
//      }
//
//      private[this] def checkTotalsIsNotSmaller(event: ScoreEvent, prev: ScoreEvent) = {
//        var validations = Seq.empty[ScoreEventValidationException]
//        if (event.team1Total.points < prev.team1Total.points)
//          validations =
//            validations :+ ScoreEventTeamTotalsValidationException(Team1, event.team1Total, prev.team1Total)
//        if (event.team2Total.points < prev.team2Total.points)
//          validations =
//            validations :+ ScoreEventTeamTotalsValidationException(Team2, event.team2Total, prev.team2Total)
//
//        validations
//      }
//
//      private[this] def validatePointsIncreaseAndTotals(event: ScoreEvent, prev: ScoreEvent) = {
//        var validations = Seq.empty[ScoreEventValidationException]
//        event.scoringTeam match {
//          case Team1 =>
//            if (prev.team1Total.points + event.pointsScored.points != event.team1Total.points)
//              validations = validations :+ ScoreEventTeamScoreIncreaseValidationException(Team1, event.team1Total, prev.team1Total, event.pointsScored)
//
//          case _ =>
//            if (prev.team2Total.points + event.pointsScored.points != event.team2Total.points)
//              validations = validations :+ ScoreEventTeamScoreIncreaseValidationException(Team1, event.team2Total, prev.team2Total, event.pointsScored)
        }

    }

//  def validate(event: ScoreEvent, prev: ScoreEvent): ZIO[ScoreEventValidator, ScoreEventValidationExceptions, ScoreEvent] =
//    ZIO.accessM(_.get.validate(event, prev))
}
