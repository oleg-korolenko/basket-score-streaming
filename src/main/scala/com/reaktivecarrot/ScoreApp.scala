package com.reaktivecarrot

import com.reaktivecarrot.decoder.ScoreEventDecoder
import com.reaktivecarrot.decoder.ScoreEventDecoder.ScoreEventDecoder
import com.reaktivecarrot.domain.{ScoreBox, ScoreEvent}
import com.reaktivecarrot.exception.ScoreAppException
import com.reaktivecarrot.exception.ScoreAppException._
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidator
import zio.clock.Clock
import zio.console._
import zio.stream.ZStream
import zio._
import com.reaktivecarrot.storage.ScoreBoxService
import com.reaktivecarrot.storage.ScoreBoxService.ScoreBoxService

object Env {
  type SysDeps = Console with Clock

  val emptyScoreBox: ZLayer[Any, Nothing, Has[Ref[ScoreBox]]] = Ref.make(ScoreBox()).toLayer
  val validator: ZLayer[Any, Nothing, ScoreEventValidator]    = emptyScoreBox >>> ScoreEventValidator.live
  val scoreBoxService: ZLayer[Any, Nothing, ScoreBoxService]  = emptyScoreBox >>> ScoreBoxService.inMemory

  type AppEnvironment = SysDeps with ScoreEventDecoder

  val live = Console.live ++ Clock.live ++ ScoreEventDecoder.live ++ validator ++ scoreBoxService
}

object ScoreApp extends zio.App {

  def run(scoreEvents: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] = {

    def logError(data: String) = {
      putStrLnErr(s" Error: $data") *> Task.succeed()
    }

    def log(data: String) = {
      putStrLn(s" Event: $data") *> Task.succeed()
    }

    val inStream: ZStream[Any, Nothing, String] = ZStream("", "0x781002", "0xf0101f", "0x781002")

    val program =
      inStream
        .via(ScoreEventDecoder.decode)
        .via(ScoreEventValidator.validate[ScoreEventDecoder])
        .via(ScoreBoxService.add[ScoreEventDecoder with ScoreEventValidator])
        .partition(_.isLeft)
        .use {
          case (errors, events) =>
            val errorsIO: ZIO[Console, Nothing, Chunk[ScoreAppException]] = errors
              .filter(_.isLeft)
              .collectWhile { case Left(a) => a }
              .tap(e => logError(e.message))
              .runCollect

            val successIO = events
              .filter(_.isRight)
              .collectWhile { case Right(a) => a }
              .tap(e => log(e.toString))
              .runCollect

            successIO &> errorsIO
        }

    program
      .provideLayer(Env.live)
      .exitCode

  }

}
