package com.reaktivecarrot

import zio.ExitCode
import zio.test.Assertion.equalTo
import zio.test.{assertM, suite, testM, DefaultRunnableSpec}

object ScoreAppSpec extends DefaultRunnableSpec {

  def spec =
    suite(" ScoreApp")(
      testM("args : should  fail missing mandatory score file path argument") {
        assertM(ScoreApp.run(List()))(equalTo(ExitCode.failure))
      },
      testM("args: should  fail having more arguments") {
        assertM(ScoreApp.run(List("empty.txt", "another-file.txt")))(equalTo(ExitCode.failure))
      },
      testM("should return failure trying to parse not existing file") {
        assertM(ScoreApp.run(List("not-existing-file.txt")))(equalTo(ExitCode.failure))
      },
      testM("should  parse 0 events from an empty file") {
        assertM(ScoreApp.run(List("empty.txt")))(equalTo(ExitCode.success))
      },
      testM("should successfully parse all events from a file containing correct scores") {
        assertM(ScoreApp.run(List("happy-path.txt")))(equalTo(ExitCode.success))
      },
      testM("should successfully parse all events from a file containing multiple problems") {
        assertM(ScoreApp.run(List("not-so-happy-path.txt")))(equalTo(ExitCode.success))
      }
    )
}
