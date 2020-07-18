package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain._
import com.reaktivecarrot.domain.exception.ScoringEventDecodeException
import zio.{Has, Layer, ZIO, ZLayer}

object ScoringEventDecoder {

  type ScoringEventDecoder = Has[ScoringEventDecoder.Service]

  trait Service {
    def decode(hex: String): ZIO[Any, ScoringEventDecodeException, ScoringEvent]

  }

  val live: Layer[Nothing, ScoringEventDecoder] = ZLayer.succeed(new Service {

    override def decode(hex: String): ZIO[Any, ScoringEventDecodeException, ScoringEvent] = {

      ZIO.fromEither {
        try {
          val intHexInput    = Integer.decode(hex).toInt
          val binaryStrInput = intHexInput.toBinaryString
          val padded         = f"$binaryStrInput%32s".replace(' ', '0')
          Right(
            ScoringEvent(
              matchTime = MatchTimeInSecs(Integer.parseInt(padded.slice(1, 13), 2)),
              team1Total = TeamPointsTotal(Integer.parseInt(padded.substring(13, 21), 2)),
              team2Total = TeamPointsTotal(Integer.parseInt(padded.substring(21, 29), 2)),
              scoringTeam = ScoringTeam.getTeam(Integer.parseInt(padded.substring(29, 30), 2)),
              pointsScored = PointsScored(Integer.parseInt(padded.substring(30, 32), 2))
            )
          )
        } catch {
          case e: Throwable => {
            println(s"yahoo for $hex")
            Left(ScoringEventDecodeException(hex))
          }
        }
      }
    }
  })

  // val test: Layer[Nothing, ScoringEventDecoder] = ???

  def decode(hex: String): ZIO[ScoringEventDecoder, ScoringEventDecodeException, ScoringEvent] =
    ZIO.accessM(_.get.decode(hex))
}
