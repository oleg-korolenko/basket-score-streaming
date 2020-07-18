package com.reaktivecarrot

package object domain {

  final case class MatchTimeInSecs(value: Int) extends AnyVal
  final case class TeamPointsTotal(value: Int) extends AnyVal

  sealed trait ScoringTeam { def team: Int }
  case object Team1 extends ScoringTeam { val team = 0 }
  case object Team2 extends ScoringTeam { val team = 1 }

  object ScoringTeam {

    def getTeam(team: Int): ScoringTeam =
      team match {
        case 0               => Team1
        case 1               => Team2
        case notSupportedVal => throw new RuntimeException(s"$notSupportedVal  is not supported as a team value")
      }

  }
  final case class PointsScored(value: Int) extends AnyVal

  final case class ScoringEvent(
    pointsScored: PointsScored,
    scoringTeam: ScoringTeam,
    team2Total: TeamPointsTotal,
    team1Total: TeamPointsTotal,
    matchTime: MatchTimeInSecs
  )

}
