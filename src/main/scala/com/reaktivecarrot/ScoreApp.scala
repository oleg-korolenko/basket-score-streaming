package com.reaktivecarrot

import com.reaktivecarrot.decoder.ScoreEventDecoder
import com.reaktivecarrot.decoder.ScoreEventDecoder.ScoreEventDecoder
import com.reaktivecarrot.domain.ScoreEvent
import com.reaktivecarrot.exception.ScoreAppException
import com.reaktivecarrot.storage.ScoreKeeper.ScoreKeeper
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidator
import zio.clock.Clock
import zio.console._
import zio.stream.ZStream
import zio._

object Env {
  type SysDeps = Console with Clock

  val emptyScoreBox: Layer[Nothing, Has[ScoreBox]] = ZLayer.fromEffect(Ref.make(Vector.empty[ScoreEvent]))
//  val validator: Layer[Nothing, ScoreEventValidator] = emptyScoreBox >>> ScoreEventValidator.live
  //val scoreKeeper: ZLayer[Has[Ref[ScoreBox]], Nothing, ScoreKeeper] = emptyScoreBox >>> ScoreKeeper.inMemory

  type AppEnvironment = SysDeps with ScoreEventDecoder with ScoreKeeper
  //++ scoreKeeper
  val live = Console.live ++ Clock.live ++ ScoreEventDecoder.live
}

object ScoreApp extends zio.App {

  def run(scoreEvents: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {

    val inStream: ZStream[Any, Nothing, String] = ZStream("", "0x781002", "0xf0101f")

    val maybeDecoded: ZStream[ScoreEventDecoder, Nothing, Either[ScoreAppException.ScoreEventDecodeException, ScoreEvent]] =
      inStream.via(ScoreEventDecoder.decode)

    val decoded: ZStream[ScoreEventDecoder, Nothing, Either[ScoreAppException.ScoreEventDecodeException, ScoreEvent]] = maybeDecoded.filter(_.isRight)
    val failedToBeDecoded                                                                                             = maybeDecoded.filter(_.isLeft)

    case class Whatever(msg: String)

    val program =
      decoded
        .tap(event => putStr(event.toString))
        .runCollect

    program.provideLayer(Env.live).exitCode

  }

}
