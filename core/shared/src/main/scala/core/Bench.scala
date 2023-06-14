package core

import cats.syntax.all._
import fs2.io.file.Files
import fs2.io.file.Path
import fs2.Stream
import cats.effect.kernel.Async
import cats.effect.Resource

class Bench[F[_]](
    val benchFiles: List[Path]
) {}

object Bench {
  def mkBench[F[_]: Async](workDir: Path): Resource[F, Bench[F]] =
    Resource.make[F, Bench[F]] {

      val s1 = Files[F]
        .readAll(
          workDir / "project" / "plugins.sbt"
        )
        .through(fs2.text.utf8.decode)
        .filter(_.contains("sbt-jmh"))
        .handleErrorWith(t => Stream.raiseError[F](new RuntimeException("jmh plugin not found")))

      val s2 = Files[F]
        .walk(workDir)
        .evalFilter { path =>
          Files[F]
            .isDirectory(path)
            .map(!_)
        }
        .flatMap { path =>
          Files[F]
            .readAll(path)
            .through(fs2.text.utf8.decode)
            .map(content => (path, content))
        }
        .filter { case (path, content) =>
          content.contains("@Benchmark")
        }
        .handleErrorWith(t =>
          Stream.raiseError[F](new RuntimeException("benchmark files not found"))
        )

      s1
        .flatMap { _ =>
          s2.map(_._1)
        }
        .compile
        .toList
        .map(new Bench[F](_))

    }(_ => Async[F].unit)

}
