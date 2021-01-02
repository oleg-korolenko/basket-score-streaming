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
      putStrLn(s" Error message: ${data}") *> Task.succeed()
    }

    val inStream: ZStream[Any, Nothing, String] = ZStream("", "0x781002", "0xf0101f")

    val maybeDecoded  = inStream.via(ScoreEventDecoder.decode)
    val decodedStream = maybeDecoded.filter(_.isRight).collectWhile { case Right(a) => a }

    // val maybeValidated = decodedStream.via(ScoreEventValidator.validate)

    // error channels
    val decodeErrorsStream = maybeDecoded.filter(_.isLeft).collectWhile { case Left(a) => a }

    val program =
      inStream
        .via(ScoreEventDecoder.decode)
        .via(ScoreEventValidator.validate[ScoreEventDecoder])
        .via(ScoreBoxService.add[ScoreEventDecoder with ScoreEventValidator])
        .tap(e => putStr(s" here $e"))
        .partition(_.isLeft)
        .use {
          case (errors, events) =>
            val errorsIO: ZIO[Console, Nothing, Chunk[ScoreAppException]] = errors
              .filter(_.isLeft)
              .collectWhile { case Left(a) => a }
              //              .tap(logError)
              .runCollect

            val successIO = events
              .filter(_.isRight)
              .collectWhile { case Right(a) => a }
              //              .tap(log)
              .runCollect

            successIO &> errorsIO
        }

    program
      .provideLayer(Env.live)
      .exitCode

  }

}
