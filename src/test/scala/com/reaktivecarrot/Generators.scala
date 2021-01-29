package com.reaktivecarrot

import com.reaktivecarrot.domain.{MatchTimeInSecs, PointsScored, PointsScored1, PointsScored3, ScoreEvent, Team, Team1, Team2, TeamPointsTotal}
import zio.random.Random
import zio.test.Gen

object Generators {

  val matchTimeGen: Gen[Random, MatchTimeInSecs] =
    for {
      matchTime <- Gen.int(1, 80 * 60)
    } yield MatchTimeInSecs(matchTime)

  val teamGen: Gen[Random, Team]                 = Gen.elements(Team1, Team2)
  val pointsScoredGen: Gen[Random, PointsScored] = Gen.elements(PointsScored1, PointsScored1, PointsScored3)
  val totalPointGen: Gen[Random, Int]            = Gen.int(0, 150)

  val scoreEventGen: Gen[Random, ScoreEvent] =
    for {
      points: PointsScored <- pointsScoredGen
      scoringTeam: Team    <- teamGen
      team1Total <- totalPointGen.map { genPoints =>
        if (scoringTeam == Team1) genPoints + points.points
        else genPoints
      }
      team2Total <- totalPointGen.map { genPoints =>
        if (scoringTeam == Team2) genPoints + points.points
        else genPoints
      }
      matchTime <- matchTimeGen
    } yield ScoreEvent(points, scoringTeam, TeamPointsTotal(team1Total), TeamPointsTotal(team2Total), matchTime)

}
