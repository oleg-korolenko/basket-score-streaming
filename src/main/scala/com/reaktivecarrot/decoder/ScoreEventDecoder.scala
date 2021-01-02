package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception.ScoreAppException
import com.reaktivecarrot.exception.ScoreAppException._
import zio.{Has, IO, Layer, ZIO, ZLayer}
import zio.stream.ZStream

object ScoreEventDecoder {

  type ScoreEventDecoder = Has[Service]

  trait Service {
    def decode(events: EncodedEventsStream): ZStream[Any, Nothing, ScoreEventOr[ScoreAppException]]
  }

  val live: Layer[Nothing, ScoreEventDecoder] = ZLayer.succeed(new Service {

    override def decode(encodedEvents: EncodedEventsStream): ZStream[Any, Nothing, ScoreEventOr[ScoreAppException]] =
      encodedEvents.map[Either[ScoreEventDecodeException, ScoreEvent]] {
        encoded =>
          try {
            val intHexInput    = Integer.decode(encoded).toInt
            val binaryStrInput = intHexInput.toBinaryString
            val padded         = f"$binaryStrInput%32s".replace(' ', '0')
            // we return a new 1 entry stream with ScoreEvent
            Right {
              ScoreEvent(
                matchTime = MatchTimeInSecs(Integer.parseInt(padded.slice(1, 13), 2)),
                team1Total = TeamPointsTotal(Integer.parseInt(padded.substring(13, 21), 2)),
                team2Total = TeamPointsTotal(Integer.parseInt(padded.substring(21, 29), 2)),
                scoringTeam = Team.getTeam(Integer.parseInt(padded.substring(29, 30), 2)),
                pointsScored = PointsScored.getPoints(Integer.parseInt(padded.substring(30, 32), 2))
              )
            }

          } catch {
            // if we fail we return a new 1 entry failed stream with ScoreEventDecodeException
            case _: Throwable => Left(ScoreEventDecodeException(encoded))
          }
      }
  })

  def decode(encodedEvents: EncodedEventsStream): ZStream[ScoreEventDecoder, Nothing, ScoreEventOr[ScoreAppException]] =
    ZStream.accessStream[ScoreEventDecoder](_.get.decode(encodedEvents))

}
