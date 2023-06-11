package core

import cats.effect._
import fs2.io.process._
import fs2.io.file.Files

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    // resourceの調達
    //   gitを確認.
    //     git commandがあるか？
    //     実行dirに.gitがあるか？
    //   benchmarkfileを確認
    //     ファイル数に応じて、並列に実行したい(そういうことってできるっけ？ なるべくsbtに依存したくない)
    //
    // actionの実行
    //   benchmarkをparallelに実行

    val resource =
      Git.mkGit[IO].flatMap { g =>
        Bench.mkBench[IO].flatMap(b => Resource.pure((g, b)))
      }

    resource.use { case (g, b) =>
      ProcessBuilder.apply("git", "diff", "--name-only").spawn[IO].use[Unit] { process =>
        process.stdout
          .through(fs2.text.utf8.decode)
          .compile
          .string
          .flatMap(IO.println(_))
      }
    }
  }
}
