package com.reaktivecarrot

import com.reaktivecarrot.decoder.ScoreEventDecoder
import com.reaktivecarrot.exception.ScoreAppException
import com.reaktivecarrot.storage.ScoreKeeper
import com.reaktivecarrot.storage.ScoreKeeper.{ScoreBox, ScoreKeeper}
import com.reaktivecarrot.validation.ScoreEventValidator
import com.reaktivecarrot.validation.ScoreEventValidator.ScoreEventValidator
import com.reaktivecarrot.decoder.ScoreEventDecoder.{ScoreEventDecoder, _}
import com.reaktivecarrot.domain.ScoreEvent
import com.reaktivecarrot.exception.ScoreAppException.ScoreEventDecodeException
import zio.{ExitCode, Has, ZIO}

object ScoreApp extends zio.App {

// TODO add dep on Logger

  def run(scoreEvents: List[String]) = ???

  def program(encodedEvents: List[String]): ZIO[ScoreEventDecoder with ScoreEventValidator with ScoreKeeper, ScoreAppException, ScoreBox] =
    for {
      decoded: ZIO[ScoreEventDecoder, ScoreEventDecodeException, ScoreEvent] <- decodeEvents(encodedEvents)
      decodedR                                                               <- decoded

      validated <- ScoreEventValidator.validate(decoded)
    } yield ()

  private[this] def decodeEvents(scoreEvents: List[String]): List[ZIO[ScoreEventDecoder, ScoreEventDecodeException, ScoreEvent]] = {
    scoreEvents.map(ScoreEventDecoder.decode)
  }

}
