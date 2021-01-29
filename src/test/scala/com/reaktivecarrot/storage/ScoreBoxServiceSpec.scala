package com.reaktivecarrot.storage

import com.reaktivecarrot.Generators._
import com.reaktivecarrot.domain._
import com.reaktivecarrot.storage.ScoreBoxService.ScoreBoxService
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test._
import zio.{Ref, UIO}

object ScoreBoxServiceSpec extends DefaultRunnableSpec {

  def spec =
    suite("ScoreBoxService")(
      suite("adding scoring events")(
        testM("should add one score event") {
          checkM(Gen.oneOf(scoreEventGen)) { event =>
            val scoreBox: UIO[Ref[ScoreBox]] = Ref.make(ScoreBox())
            val result =
              ScoreBoxService
                .add[ScoreBoxService](ZStream(Right(event)))
                .provideLayer(scoreBox.toLayer >>> ScoreBoxService.inMemory)
                .runHead

            assertM(result)(isSome(equalTo(Right(event))))
          }
        },
        testM("should add multiple score events") {
          checkM(Gen.vectorOf(scoreEventGen).map(_.map(Right(_)))) { events =>
            val scoreBoxIO: UIO[Ref[ScoreBox]] = Ref.make(ScoreBox())

            val result = ScoreBoxService
              .add[ScoreBoxService](ZStream.fromIterable(events))
              .provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory)
              .runCollect
            val expected = zio.Chunk(events: _*)
            assertM(result)(equalTo(expected))
          }
        }
      ),
      suite("retrieving scoring events")(
        testM("should return 2 out of 2 existing events when requesting 2") {
          checkM(Gen.vectorOfN(2)(scoreEventGen)) { events =>
            val scoreBox                        = ScoreBox(events)
            val scoreBoxIO: UIO[Ref[ScoreBox]]  = Ref.make(scoreBox)
            val expected: zio.Chunk[ScoreEvent] = zio.Chunk(events: _*)

            val result = ScoreBoxService.take(2).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

            assertM(result)(equalTo(expected))
          }
        },
        testM("should return last  1 out of 2 existing events when requesting 1") {
          checkM(Gen.vectorOfN(2)(scoreEventGen)) { events =>
            val scoreBox                        = ScoreBox(events)
            val scoreBoxIO: UIO[Ref[ScoreBox]]  = Ref.make(scoreBox)
            val expected: zio.Chunk[ScoreEvent] = zio.Chunk(events.tail.head)

            val result = ScoreBoxService.take(1).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

            assertM(result)(equalTo(expected))
          }
        },
        testM("should return 1 out of 1 existing events when requesting 2") {
          checkM(Gen.oneOf(scoreEventGen)) { event =>
            val scoreBox   = ScoreBox(Vector(event))
            val scoreBoxIO = Ref.make(scoreBox)

            val expected = zio.Chunk(event)

            val result = ScoreBoxService.take(2).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

            assertM(result)(equalTo(expected))
          }
        },
        testM("should return empty chunk out of 0 existing events when requesting 1") {
          val scoreBox   = ScoreBox(Vector.empty[ScoreEvent])
          val scoreBoxIO = Ref.make(scoreBox)

          val expected = zio.Chunk.empty

          val result = ScoreBoxService.take(2).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
        }
      ),
      suite("last scoring event")(
        testM("should return empty chunk out of 0 existing event") {
          val scoreBox   = ScoreBox(Vector.empty[ScoreEvent])
          val scoreBoxIO = Ref.make(scoreBox)

          val expected = zio.Chunk(None)

          val result = ScoreBoxService.last().provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
        },
        testM("should return last event out of 2 existing events") {
          checkM(Gen.oneOf(scoreEventGen)) { event =>
            val scoreBox   = ScoreBox(Vector(event))
            val scoreBoxIO = Ref.make(scoreBox)

            val expected = zio.Chunk(Some(event))

            val result = ScoreBoxService.last().provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

            assertM(result)(equalTo(expected))
          }
        }
      )
    )
}
