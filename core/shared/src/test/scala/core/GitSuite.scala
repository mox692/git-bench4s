package core

import cats.effect._
import munit.CatsEffectSuite
import fs2.io.file.Path
import fs2.io.file.Files
import fs2.io.process._
import cats.syntax.all._

class GitSuite extends CatsEffectSuite {

  private def runGitDir[A](cb: Path => IO[A]): IO[A] =
    Files[IO].tempDirectory
      .flatMap { tmpDir =>
        val gitPath = Path(s"${tmpDir.absolute.toString}/.git")

        Resource.make {
          Files[IO]
            .createDirectories(
              gitPath,
              None
            )
            .flatMap { _ =>
              IO.pure(tmpDir)
            }
        }(_ => Files[IO].delete(gitPath))
      }
      .use { tmpDir =>
        cb(tmpDir)
      }

  private def runNoGitDir[A](cb: Path => IO[A]): IO[A] =
    Files[IO].tempDirectory
      .use { tmpDir =>
        cb(tmpDir)
      }

  test("Git.isInitialized method should return false where there is no .git directory") {
    runNoGitDir { path =>
      Git.isInitialized[IO](path).assertEquals(false)
    }
  }

  test("Git.isInitialized method should return true where there is a .git directory") {
    runGitDir { path =>
      Git.isInitialized[IO](path).assertEquals(true)
    }
  }

  test("Git.isInitialized method should success where there is a .git directory") {
    runGitDir { path =>
      Git.mkGit[IO](path).use(_ => IO.unit).void.assertEquals(())
    }
  }

  test("Git.isInitialized method should fail where there is no .git directory") {
    runNoGitDir { path =>
      Git
        .mkGit[IO](path)
        .use(_ => IO.unit)
        .handleErrorWith { case _: RuntimeException => IO.unit }
        .assertEquals(())
    }
  }
}
