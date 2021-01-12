package com.reaktivecarrot

import com.reaktivecarrot.decoder.ScoreEventDecoder
import com.reaktivecarrot.decoder.ScoreEventDecoder.ScoreEventDecoder
import com.reaktivecarrot.domain.ScoreBox
import com.reaktivecarrot.storage.ScoreBoxService
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidator
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.stream.{ZStream, ZTransducer}

import java.nio.file.Paths

object Env {
  type SysDeps = Console with Clock

  val emptyScoreBox   = Ref.make(ScoreBox()).toLayer
  val validator       = emptyScoreBox >>> ScoreEventValidator.live
  val scoreBoxService = emptyScoreBox >>> ScoreBoxService.inMemory

  type AppEnvironment = SysDeps with ScoreEventDecoder

  val live = Blocking.live ++ Console.live ++ Clock.live ++ ScoreEventDecoder.live ++ validator ++ scoreBoxService
}

object ScoreApp extends zio.App {

  def logError(data: String) = {
    putStrLnErr(s" Error: $data") *> Task.succeed()
  }

  def log(data: String) = {
    putStrLn(s" Event: $data") *> Task.succeed()
  }

  def run(args: List[String]) = {

    args match {
      case scoreFilePath :: Nil =>
        val fileInputStream: ZStream[Blocking, Throwable, String] = ZStream
          .fromFile(Paths.get(ClassLoader.getSystemResource(scoreFilePath).toURI))
          .aggregate(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
//          .cat
        val program =
          fileInputStream
            .via(ScoreEventDecoder.decode[Blocking])
            .via(ScoreEventValidator.validate[Blocking with ScoreEventDecoder])
            .via(ScoreBoxService.add[Blocking with ScoreEventDecoder with ScoreEventValidator])
            .partition(_.isLeft)
            .use {
              case (errors, events) =>
                val errorsIO = errors
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
      case _ => console.putStrLn(" [score events path] should be provided as argument").as(ExitCode.failure)
    }

  }

}
