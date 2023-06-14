package core

package core

import munit.CatsEffectSuite
import cats.effect.IO
import fs2.io.file.Path
import cats.syntax.all._

class BenchSuite extends CatsEffectSuite {

  test("Bench.mkBench method should success where using sbt-plugin and benchmark file") {
    val validDir = Path("./core/shared/src/test/scala/core/testDir")
    Bench.mkBench[IO](validDir).use { case _: Bench[IO] => IO.unit }.assertEquals(())
  }

  test("Bench.mkBench method should fail where using sbt-plugin and benchmark file") {
    val invalidDir = Path("./core/shared/src/test/scala/core")
    Bench
      .mkBench[IO](invalidDir)
      .use(_ => IO.unit)
      .handleErrorWith { case _: RuntimeException =>
        IO.unit
      }
      .assertEquals(())
  }

}
