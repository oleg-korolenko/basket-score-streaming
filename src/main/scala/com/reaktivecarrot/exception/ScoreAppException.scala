package com.reaktivecarrot.exception

import com.reaktivecarrot.domain.{MatchTimeInSecs, PointsScored, ScoreEvent, Team, TeamPointsTotal}

sealed trait ScoreAppException extends Throwable {
  def message: String
  override def getMessage: String = message
}

sealed trait ScoreEventValidationException extends ScoreAppException

object ScoreAppException {

  final case class ScoreEventDecodeException(input: String) extends ScoreAppException {
    def message: String = s"$input is not decodable"
  }

  final case class ScoreBoxAddException(event: ScoreEvent, cause: String) extends ScoreAppException {
    def message: String = s"$event can't be added due to $cause"
  }

  final case class ScoreBoxRetrievalException(elems: Int) extends ScoreAppException {
    def message: String = s"Can't retrieve $elems from scores"
  }

  final case class ScoreEventMatchTimeValidationException(matchTime: MatchTimeInSecs, prevMatchTime: MatchTimeInSecs) extends ScoreEventValidationException {
    def message: String = s"Match time of the current event ${matchTime.secs}  can't be smaller than prev one ${prevMatchTime.secs}"
  }

  final case class ScoreEventTeamTotalsValidationException(team: Team, totals: TeamPointsTotal, prevTotals: TeamPointsTotal)
      extends ScoreEventValidationException {
    def message: String = s"$team totals of the current event ${totals.points}  can't be smaller than prev one ${prevTotals.points}"
  }

  final case class ScoreEventTeamScoreIncreaseValidationException(team: Team, totals: TeamPointsTotal, prevTotals: TeamPointsTotal, pointsScored: PointsScored)
      extends ScoreEventValidationException {
    def message: String = s"$team totals ${totals.points} don't match increase of ${pointsScored.points} from prev ${prevTotals.points}"
  }
}
