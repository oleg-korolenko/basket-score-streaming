package com.reaktivecarrrot.validator

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception.ScoreAppException._
import com.reaktivecarrot.validation.ScoreEventValidator
import zio.test.Assertion._
import zio.test.{testM, _}

object ScoreEventValidatorSpec extends DefaultRunnableSpec {

  def spec =
    suite(" ScoreEventValidator")(
      testM("should return ScoreEventValidationException if match time is < than prev") {
        val prev         = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
        val currentEvent = prev.copy(matchTime = MatchTimeInSecs(30), team1Total = TeamPointsTotal(2))
        val expected     = Seq(ScoreEventMatchTimeValidationException(currentEvent.matchTime, prev.matchTime))

        val result = ScoreEventValidator.validate(currentEvent, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return ScoreEventValidationException if team1 totals  < than prev") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
        val team2ScoresButTeam1TotalIsSmallerThanBefore = prev
          .copy(pointsScored = PointsScored1, scoringTeam = Team2, team2Total = TeamPointsTotal(1), team1Total = TeamPointsTotal(0))
        val expected = Seq(ScoreEventTeamTotalsValidationException(Team1, team2ScoresButTeam1TotalIsSmallerThanBefore.team1Total, prev.team1Total))

        val result = ScoreEventValidator.validate(team2ScoresButTeam1TotalIsSmallerThanBefore, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return ScoreEventValidationException if team2 totals  < than prev") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(2), TeamPointsTotal(2), MatchTimeInSecs(60))
        val team1ScoresButTeam2TotalIsSmallerThanBefore = prev
          .copy(pointsScored = PointsScored1, scoringTeam = Team1, team2Total = TeamPointsTotal(1), team1Total = TeamPointsTotal(3))
        val expected = Seq(ScoreEventTeamTotalsValidationException(Team2, team1ScoresButTeam2TotalIsSmallerThanBefore.team2Total, prev.team2Total))

        val result = ScoreEventValidator.validate(team1ScoresButTeam2TotalIsSmallerThanBefore, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return ScoreEventValidationException if team1 scores but totals are not correct for team1") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(2), TeamPointsTotal(2), MatchTimeInSecs(60))
        val team1Scores1 =
          prev.copy(
            matchTime = MatchTimeInSecs(90),
            pointsScored = PointsScored1,
            scoringTeam = Team1,
            team1Total = TeamPointsTotal(99),
            team2Total = TeamPointsTotal(2)
          )
        val expected = Seq(ScoreEventTeamScoreIncreaseValidationException(Team1, team1Scores1.team1Total, prev.team1Total, team1Scores1.pointsScored))

        val result = ScoreEventValidator.validate(team1Scores1, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return ScoreEventValidationException if team2 scores but totals are not correct for team2") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(2), TeamPointsTotal(2), MatchTimeInSecs(60))
        val team2Scores1 =
          prev.copy(
            matchTime = MatchTimeInSecs(90),
            pointsScored = PointsScored1,
            scoringTeam = Team2,
            team2Total = TeamPointsTotal(99),
            team1Total = TeamPointsTotal(2)
          )
        val expected = Seq(ScoreEventTeamScoreIncreaseValidationException(Team1, team2Scores1.team2Total, prev.team2Total, team2Scores1.pointsScored))

        val result = ScoreEventValidator.validate(team2Scores1, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return all encountered validation exceptions") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(2), TeamPointsTotal(2), MatchTimeInSecs(60))
        val currentEvent = prev.copy(
          pointsScored = PointsScored1,
          scoringTeam = Team1,
          matchTime = MatchTimeInSecs(30),
          team1Total = TeamPointsTotal(1),
          team2Total = TeamPointsTotal(1)
        )
        val expected = Seq(
          ScoreEventMatchTimeValidationException(currentEvent.matchTime, prev.matchTime),
          ScoreEventTeamTotalsValidationException(Team1, currentEvent.team1Total, prev.team1Total),
          ScoreEventTeamTotalsValidationException(Team2, currentEvent.team2Total, prev.team2Total),
          ScoreEventTeamScoreIncreaseValidationException(Team1, currentEvent.team1Total, prev.team1Total, currentEvent.pointsScored)
        )

        val result = ScoreEventValidator.validate(currentEvent, prev).provideLayer(ScoreEventValidator.live)

        assertM(result.flip)(equalTo(expected))
      },
      testM("should return event itself if fully valid") {
        val prev = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(2), TeamPointsTotal(2), MatchTimeInSecs(60))
        val team1Scores1 =
          prev.copy(
            matchTime = MatchTimeInSecs(90),
            pointsScored = PointsScored1,
            scoringTeam = Team1,
            team1Total = TeamPointsTotal(3),
            team2Total = TeamPointsTotal(2)
          )
        val expected = team1Scores1

        val result = ScoreEventValidator.validate(team1Scores1, prev).provideLayer(ScoreEventValidator.live)

        assertM(result)(equalTo(expected))
      }
    )

}
