package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception.ScoreAppException._
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test._

object ScoreEventDecoderSpec extends DefaultRunnableSpec {

  def spec =
    suite("ScoreEventDecoder")(
      testM("should return ScoringEventDecodeException for empty string as input") {
        val input = ""

        val result =
          ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        assertM(result)(isSome(isLeft(equalTo(ScoreEventDecodeException(input)))))
      },
      testM("should return ScoringEventDecodeException for nox hex string as input") {
        val input = "---"

        val result = ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        assertM(result)(isSome(isLeft(equalTo(ScoreEventDecodeException(input)))))
      },
      testM("should decode a scoring event : At 00:15, Team 1 scores 2, score 2:0") {
        val input = "0x781002"

        val result =
          ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        val expected = ScoreEvent(
          pointsScored = PointsScored2,
          scoringTeam = Team1,
          team2Total = TeamPointsTotal(0),
          team1Total = TeamPointsTotal(2),
          matchTime = MatchTimeInSecs(15)
        )

        assertM(result)(isSome(isRight(equalTo(expected))))
      },
      testM("should decode a scoring event : At 00:30,  Team 2 scores 3, score 2:3") {
        val input = "0xf0101f"

        val result = ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        val expected =
          ScoreEvent(
            matchTime = MatchTimeInSecs(30),
            team1Total = TeamPointsTotal(2),
            team2Total = TeamPointsTotal(3),
            scoringTeam = Team2,
            pointsScored = PointsScored3
          )

        assertM(result)(isSome(isRight(equalTo(expected))))
      },
      testM("should decode a scoring event : At 10:10, Team 1 scores 1, score 25:20 ") {
        val input = "0x1310c8a1"

        val result = ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead
        val expected = ScoreEvent(
          matchTime = MatchTimeInSecs(610),
          team1Total = TeamPointsTotal(25),
          team2Total = TeamPointsTotal(20),
          scoringTeam = Team1,
          pointsScored = PointsScored1
        )

        assertM(result)(isSome(isRight(equalTo(expected))))
      },
      testM("should decode a scoring event : At 22:23, Team 1 scores 2, score 48:52 ") {
        val input = "0x29f981a2"

        val result = ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        val expected = ScoreEvent(
          matchTime = MatchTimeInSecs(22 * 60 + 23),
          team1Total = TeamPointsTotal(48),
          team2Total = TeamPointsTotal(52),
          scoringTeam = Team1,
          pointsScored = PointsScored2
        )

        assertM(result)(isSome(isRight(equalTo(expected))))

      },
      testM("should decode a scoring event : At 38:30, Team 2 scores 3, score 100:100") {
        val input = "0x48332327"

        val result = ScoreEventDecoder.decode(ZStream(input)).provideLayer(ScoreEventDecoder.live).runHead

        val expected = ScoreEvent(
          matchTime = MatchTimeInSecs(38 * 60 + 30),
          team1Total = TeamPointsTotal(100),
          team2Total = TeamPointsTotal(100),
          scoringTeam = Team2,
          pointsScored = PointsScored3
        )

        assertM(result)(isSome(isRight(equalTo(expected))))
      }
//      testM("just checking ") {
//        import zio.console._
//
//        val a: ZStream[zio.console.Console, Nothing, Int]              = ZStream(1, 2, 3)
//        def multiply(ints: ZStream[zio.console.Console, Nothing, Int]) = ints.map(_ * 2)
//
//        type State = Has[Vector[Int]]
//        val keeper = { (state: State, event: Int) => state += 1 }
//
//        val program: ZIO[Console, Nothing, Chunk[Int]] = a
//          .via(multiply)
//          .via(keeper)
//          .tap(event => putStr(event.toString))
//          .via()
//          .runCollect
//
//        val result: URIO[Any with Console, ExitCode] = program.provideLayer(zio.console.Console.live).exitCode
//        assertM(result)(equalTo(ExitCode.success))
//      }
    )
}
