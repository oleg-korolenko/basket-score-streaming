package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain._
import com.reaktivecarrot.exception.ScoreAppException._
import zio.{Has, Layer, ZIO, ZLayer}

object ScoreEventDecoder {

  type ScoreEventDecoder = Has[ScoreEventDecoder.Service]

  trait Service {
    def decode(hex: String): ZIO[Any, ScoreEventDecodeException, ScoreEvent]

  }

  val live: Layer[Nothing, ScoreEventDecoder] = ZLayer.succeed(new Service {

    override def decode(hex: String): ZIO[Any, ScoreEventDecodeException, ScoreEvent] = {

      ZIO.fromEither {
        try {
          val intHexInput    = Integer.decode(hex).toInt
          val binaryStrInput = intHexInput.toBinaryString
          val padded         = f"$binaryStrInput%32s".replace(' ', '0')
          Right(
            ScoreEvent(
              matchTime = MatchTimeInSecs(Integer.parseInt(padded.slice(1, 13), 2)),
              team1Total = TeamPointsTotal(Integer.parseInt(padded.substring(13, 21), 2)),
              team2Total = TeamPointsTotal(Integer.parseInt(padded.substring(21, 29), 2)),
              scoringTeam = Team.getTeam(Integer.parseInt(padded.substring(29, 30), 2)),
              pointsScored = PointsScored.getPoints(Integer.parseInt(padded.substring(30, 32), 2))
            )
          )
        } catch {
          case _: Throwable => Left(ScoreEventDecodeException(hex))
        }
      }
    }
  })

  def decode(hex: String): ZIO[ScoreEventDecoder, ScoreEventDecodeException, ScoreEvent] =
    ZIO.accessM(_.get.decode(hex))
}
