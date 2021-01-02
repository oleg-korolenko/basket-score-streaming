package com.reaktivecarrot.storage

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
        testM("should add ScoreEvent events") {
          val event                        = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
          val scoreBox: UIO[Ref[ScoreBox]] = Ref.make(ScoreBox())
          val result =
            ScoreBoxService
              .add[ScoreBoxService](ZStream(Right(event)))
              .provideLayer(scoreBox.toLayer >>> ScoreBoxService.inMemory)
              .runHead

          assertM(result)(isSome(equalTo(Right(event))))

        },
        testM("should add one score event") {
          val event                        = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
          val scoreBox: UIO[Ref[ScoreBox]] = Ref.make(ScoreBox())

          val result = ScoreBoxService
            .add[ScoreBoxService](ZStream(Right(event)))
            .provideLayer(scoreBox.toLayer >>> ScoreBoxService.inMemory)
            .runHead

          assertM(result)(isSome(equalTo(Right(event))))
        },
        testM("should add multiple score events") {
          val events = Vector(
            ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60)),
            ScoreEvent(PointsScored2, Team2, TeamPointsTotal(2), TeamPointsTotal(1), MatchTimeInSecs(120))
          )
          val scoreBoxIO: UIO[Ref[ScoreBox]] = Ref.make(ScoreBox())

          val result = ScoreBoxService
            .add[ScoreBoxService](ZStream(Right(events.head), Right(events.tail.head)))
            .provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory)
            .runCollect
          val expected = zio.Chunk(Right(events.head), Right(events.tail.head))
          assertM(result)(equalTo(expected))
        }
      ),
      suite("retrieving scoring events")(
        testM("should return 2 out of 2 existing events when requesting 2") {
          val existingEvents = Vector(
            ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60)),
            ScoreEvent(PointsScored2, Team2, TeamPointsTotal(2), TeamPointsTotal(1), MatchTimeInSecs(120))
          )
          val scoreBox                       = ScoreBox(existingEvents)
          val scoreBoxIO: UIO[Ref[ScoreBox]] = Ref.make(scoreBox)

          val expected: zio.Chunk[ScoreEvent] = zio.Chunk(existingEvents.head, existingEvents.tail.head)

          val result = ScoreBoxService.take(2).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
        },
        testM("should return last  1 out of 2 existing events when requesting 1") {
          val existingEvents = Vector(
            ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60)),
            ScoreEvent(PointsScored2, Team2, TeamPointsTotal(2), TeamPointsTotal(1), MatchTimeInSecs(120))
          )
          val scoreBox                       = ScoreBox(existingEvents)
          val scoreBoxIO: UIO[Ref[ScoreBox]] = Ref.make(scoreBox)

          val expected: zio.Chunk[ScoreEvent] = zio.Chunk(existingEvents.tail.head)

          val result = ScoreBoxService.take(1).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
        },
        testM("should return 1 out of 1 existing events when requesting 2") {
          val event      = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
          val scoreBox   = ScoreBox(Vector(event))
          val scoreBoxIO = Ref.make(scoreBox)

          val expected = zio.Chunk(event)

          val result = ScoreBoxService.take(2).provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
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
          val event      = ScoreEvent(PointsScored1, Team1, TeamPointsTotal(0), TeamPointsTotal(1), MatchTimeInSecs(60))
          val scoreBox   = ScoreBox(Vector(event))
          val scoreBoxIO = Ref.make(scoreBox)

          val expected = zio.Chunk(Some(event))

          val result = ScoreBoxService.last().provideLayer(scoreBoxIO.toLayer >>> ScoreBoxService.inMemory).runCollect

          assertM(result)(equalTo(expected))
        }
      )
    )
}
