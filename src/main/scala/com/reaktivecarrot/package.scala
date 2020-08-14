package com

import com.reaktivecarrot.domain.ScoreEvent
import com.reaktivecarrot.exception.ScoreAppException.ScoreEventDecodeException
import com.reaktivecarrot.exception.ScoreEventValidationException
import zio.Ref
import zio.stream.ZStream

package object reaktivecarrot {

  type ScoreEventsStream     = ZStream[Any, Nothing, ScoreEvent]
  type DecodedEventsStream   = ZStream[Any, Nothing, Either[ScoreEventDecodeException, ScoreEvent]]
  type ValidatedEventsStream = ZStream[Any, Nothing, Either[ScoreEventValidationException, ScoreEvent]]
}
