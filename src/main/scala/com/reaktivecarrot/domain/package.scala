package com.reaktivecarrot

package object domain {

  final case class ElapsedMatchTimeInSecs(value: Int) extends AnyVal
  final case class TeamPointsTotal(value: Int)        extends AnyVal

  sealed trait ScoringTeam { def team: Short }
  case object TEAM1 extends ScoringTeam { val team = 1 }
  case object TEAM2 extends ScoringTeam { val team = 2 }

  final case class PointsScored(value: Int) extends AnyVal

  final case class ScoringEvent(
    elapsedTime: ElapsedMatchTimeInSecs,
    team1Total: TeamPointsTotal,
    team2Total: TeamPointsTotal,
    scoringTeam: ScoringTeam,
    pointsScored: PointsScored
  )

}
