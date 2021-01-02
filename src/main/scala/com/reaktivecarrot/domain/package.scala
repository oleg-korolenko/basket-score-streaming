package com.reaktivecarrot

import com.reaktivecarrot.exception.ScoreAppException.ScoreEventDecodeException
import com.reaktivecarrot.exception.{ScoreAppException, ScoreEventValidationException}
import zio.stream.ZStream

package object domain {

  final case class MatchTimeInSecs(secs: Int)   extends AnyVal
  final case class TeamPointsTotal(points: Int) extends AnyVal

  sealed trait Team { def team: Int }
  case object Team1 extends Team { val team = 0 }
  case object Team2 extends Team { val team = 1 }

  object Team {

    def getTeam(team: Int): Team =
      team match {
        case 0               => Team1
        case 1               => Team2
        case notSupportedVal => throw new RuntimeException(s"$notSupportedVal  is not supported as a team value")
      }

  }
  sealed trait PointsScored { def points: Int }
  case object PointsScored1 extends PointsScored { val points = 1 }
  case object PointsScored2 extends PointsScored { val points = 2 }
  case object PointsScored3 extends PointsScored { val points = 3 }

  object PointsScored {

    def getPoints(points: Int): PointsScored =
      points match {
        case 1               => PointsScored1
        case 2               => PointsScored2
        case 3               => PointsScored3
        case notSupportedVal => throw new RuntimeException(s"$notSupportedVal  is not supported as point that can be scored")
      }

  }

  final case class ScoreEvent(
    pointsScored: PointsScored,
    scoringTeam: Team,
    team2Total: TeamPointsTotal,
    team1Total: TeamPointsTotal,
    matchTime: MatchTimeInSecs
  )

  type ScoreEventValidationExceptions = Seq[ScoreEventValidationException]

  final case class ScoreBox(events: Vector[ScoreEvent] = Vector.empty[ScoreEvent], lastEvent: Option[ScoreEvent] = None)

  object ScoreBox {

    def apply(events: Vector[ScoreEvent]): ScoreBox =
      events match {
        case _ +: _ => ScoreBox(events, Some(events.last))
        case _      => ScoreBox(Vector.empty, None)
      }
  }

  type ScoreEventsStream[R] = ZStream[R, Nothing, ScoreEventOr[ScoreAppException]]
  type EncodedEventsStream  = ZStream[Any, Nothing, String]
  type ScoreEventOr[T]      = Either[T, ScoreEvent]
}
