package com.reaktivecarrot.decoder

import com.reaktivecarrot.domain.ScoringEvent
import zio.{Has, Layer, Task, ZLayer}
import zio.ZIO

object ScoringEventDecoder {

  final case class ScoringEventDecodeException(msg: String, underlying: Throwable) extends Throwable

  type ScoringEventDecoder = Has[ScoringEventDecoder.Service]

  trait Service {
    def decode(hex: String): ZIO[Any, ScoringEventDecodeException, ScoringEvent] = ???

  }

  val live: Layer[Nothing, ScoringEventDecoder] = ZLayer.succeed(new Service { ??? })

  val test: Layer[Nothing, ScoringEventDecoder] = ???

  def decode(hex: String): ZIO[ScoringEventDecoder, ScoringEventDecodeException, ScoringEvent] =
    ZIO.accessM(_.get.decode(hex))
}
