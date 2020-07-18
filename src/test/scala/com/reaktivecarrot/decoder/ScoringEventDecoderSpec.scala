package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain._
import zio.test._
import zio.ZIO
import zio.test.Assertion._

import com.reaktivecarrot.domain.exception.ScoringEventDecodeException

object ScoringEventDecoderSpec extends DefaultRunnableSpec {

  def spec =
    suite("decode")(
      testM("should return ScoringEventDecodeException for empty string as input") {
        val input = ""

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        assertM(result.flip)(equalTo(ScoringEventDecodeException(input)))
      },
      testM("should return ScoringEventDecodeException for null as input") {
        val input = null

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        assertM(result.flip)(equalTo(ScoringEventDecodeException(input)))
      },
      testM("should return ScoringEventDecodeException for nox hex string as input") {
        val input = "---"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        assertM(result.flip)(equalTo(ScoringEventDecodeException(input)))
      },
      testM("should decode a scoring event : At 00:15, Team 1 scores 2, score 2:0") {
        val input = "0x781002"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)
        val expected = ScoringEvent(
          pointsScored = PointsScored(2),
          scoringTeam = Team1,
          team2Total = TeamPointsTotal(0),
          team1Total = TeamPointsTotal(2),
          matchTime = MatchTimeInSecs(15)
        )

        assertM(result)(equalTo(expected))
      },
      testM("should decode a scoring event : At 00:30,  Team 2 scores 3, score 2:3") {
        val input = "0xf0101f"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        val expected = ScoringEvent(
          matchTime = MatchTimeInSecs(30),
          team1Total = TeamPointsTotal(2),
          team2Total = TeamPointsTotal(3),
          scoringTeam = Team2,
          pointsScored = PointsScored(3)
        )

        assertM(result)(equalTo(expected))
      },
      testM("should decode a scoring event : At 10:10, Team 1 scores 1, score 25:20 ") {
        val input = "0x1310c8a1"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)
        val expected = ScoringEvent(
          matchTime = MatchTimeInSecs(610),
          team1Total = TeamPointsTotal(25),
          team2Total = TeamPointsTotal(20),
          scoringTeam = Team1,
          pointsScored = PointsScored(1)
        )

        assertM(result)(equalTo(expected))
      },
      testM("should decode a scoring event : At 22:23, Team 1 scores 2, score 48:52 ") {
        val input = "0x29f981a2"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        val expected = ScoringEvent(
          matchTime = MatchTimeInSecs(22 * 60 + 23),
          team1Total = TeamPointsTotal(48),
          team2Total = TeamPointsTotal(52),
          scoringTeam = Team1,
          pointsScored = PointsScored(2)
        )

        assertM(result)(equalTo(expected))
      },
      testM("should decode a scoring event : At 38:30, Team 2 scores 3, score 100:100") {
        val input = "0x48332327"

        val result = ScoringEventDecoder.decode(input).provideLayer(ScoringEventDecoder.live)

        val expected = ScoringEvent(
          matchTime = MatchTimeInSecs(38 * 60 + 30),
          team1Total = TeamPointsTotal(100),
          team2Total = TeamPointsTotal(100),
          scoringTeam = Team2,
          pointsScored = PointsScored(3)
        )

        assertM(result)(equalTo(expected))
      }
    )
}
