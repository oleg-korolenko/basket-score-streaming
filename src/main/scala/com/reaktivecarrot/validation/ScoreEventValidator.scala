package com.reaktivecarrot.validation

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception.ScoreAppException.{
  AccValidationException,
  ScoreEventMatchTimeValidationException,
  ScoreEventTeamScoreIncreaseValidationException,
  ScoreEventTeamTotalsValidationException
}
import com.reaktivecarrot.exception.{ScoreAppException, ScoreEventValidationException}
import zio._
import zio.stream.ZStream

object ScoreEventValidator {

  type ScoreEventValidator = Has[Service]

  trait Service {
    def validate[R](events: ZStream[R, Nothing, ScoreEventOr[ScoreAppException]]): ZStream[R, Nothing, ScoreEventOr[ScoreAppException]]
  }

  val live: ZLayer[Has[Ref[ScoreBox]], Nothing, ScoreEventValidator] = ZLayer.fromService[Ref[ScoreBox], Service] { (scoreBox: Ref[ScoreBox]) =>
    new Service {
      override def validate[R](events: ZStream[R, Nothing, ScoreEventOr[ScoreAppException]]): ZStream[R, Nothing, ScoreEventOr[ScoreAppException]] =
        events.mapM {
          case Right(event) =>
            scoreBox.get.map { score =>
              score.lastEvent match {
                case None       => Right(event)
                case Some(prev) =>
                  // could have used cats.Validated or similar
                  checkMatchTimeIsNotSmaller(event, prev) ++ checkTotalsIsNotSmaller(event, prev) ++ validatePointsIncreaseAndTotals(event, prev) match {
                    case Seq()                                          => Right(event)
                    case exceptions: Seq[ScoreEventValidationException] => Left(AccValidationException(exceptions))
                  }

              }
            }
          case Left(exception: ScoreAppException) => ZIO.left(exception)
        }

    }
  }

  private[this] def checkMatchTimeIsNotSmaller(event: ScoreEvent, prev: ScoreEvent) = {
    if (event.matchTime.secs < prev.matchTime.secs) Seq(ScoreEventMatchTimeValidationException(event.matchTime, prev.matchTime))
    else Seq.empty[ScoreEventValidationException]
  }

  private[this] def checkTotalsIsNotSmaller(event: ScoreEvent, prev: ScoreEvent) = {

    var validations = Seq.empty[ScoreEventValidationException]
    if (event.team1Total.points < prev.team1Total.points)
      validations =
        validations :+ ScoreEventTeamTotalsValidationException(Team1, event.team1Total, prev.team1Total)
    if (event.team2Total.points < prev.team2Total.points)
      validations =
        validations :+ ScoreEventTeamTotalsValidationException(Team2, event.team2Total, prev.team2Total)

    validations
  }

  private[this] def validatePointsIncreaseAndTotals(event: ScoreEvent, prev: ScoreEvent) = {
    var validations = Seq.empty[ScoreEventValidationException]
    event.scoringTeam match {
      case Team1 =>
        if (prev.team1Total.points + event.pointsScored.points != event.team1Total.points)
          validations = validations :+ ScoreEventTeamScoreIncreaseValidationException(Team1, event.team1Total, prev.team1Total, event.pointsScored)

      case _ =>
        if (prev.team2Total.points + event.pointsScored.points != event.team2Total.points)
          validations = validations :+ ScoreEventTeamScoreIncreaseValidationException(Team1, event.team2Total, prev.team2Total, event.pointsScored)
    }
    validations
  }

  def validate[R](
    events: ZStream[R, Nothing, ScoreEventOr[ScoreAppException]]
  ): ZStream[R with ScoreEventValidator, Nothing, ScoreEventOr[ScoreAppException]] =
    ZStream.accessStream[R with ScoreEventValidator](_.get.validate[R](events))

}
