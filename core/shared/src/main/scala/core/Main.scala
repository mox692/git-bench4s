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
    //     jmh-pluginがあるか、bench fileがあるか
    //     ファイル数に応じて、並列に実行したい(そういうことってできるっけ？ なるべくsbtに依存したくない)
    //   process
    //
    // 実際にbenchのロジック
    //   resourceがokだったら, fork processを作成して下記を行う
    //     作業用dirを作成して、cdする
    //     所定のcommitにcheckout
    //     benchmarkを走らせる
    //     後の比較のために結果を保存
    //   親processは、本体でbenchmarkを走らせる
    //     結果を保存
    //   2つのbenchmarkの結果を比較する
    //     結果が悪化していたらalert
    //
    //
    //

    val resources =
      Git.mkGit[IO].flatMap { g =>
        Bench.mkBench[IO].flatMap { b =>
          ProcessBuilder.apply("git", "diff", "--name-only").spawn[IO].flatMap { p =>
            Resource.pure((g, b, p))
          }
        }
      }

    resources.use { case (g, b, p) =>
      p.stdout
        .through(fs.text.utf8.decode)
        .compile
        .string
        .flatMap(IO.println(_))
    }
  }
}
