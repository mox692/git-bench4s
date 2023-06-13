package core

import cats.effect.Resource
import cats.effect.kernel.Sync
import cats.syntax.all._
import fs2.io.process._
import fs2.io.file.Path
import java.lang.Runtime

class Git[F[_]] {
  def lastCommit(): F[String] = ???

  def checkout(): F[Unit] = ???

  def checkoutInOtherProcess(process: Process[F]): F[Unit] = ???
}

object Git {
  private[core] def isInitialized[F[_]: Processes](workDir: Path)(implicit F: Sync[F]): F[Boolean] =
    ProcessBuilder
      .apply("find", workDir.toString, "-maxdepth", "1", "-name", ".git")
      .withWorkingDirectory(workDir)
      .spawn[F]
      .use {
        _.stdout
          .through(fs2.text.utf8.decode)
          .compile
          .string
          .map(_.nonEmpty)
      }

  private[core] def mkGit[F[_]: Processes](
      workDir: Path
  )(implicit F: Sync[F]): Resource[F, Git[F]] =
    for {
      git <- Resource.make {
        isInitialized[F](workDir).ifM(
          Sync[F].pure(new Git[F] {}),
          Sync[F].raiseError(
            new RuntimeException("git is not initialized.")
          )
        )
      }(_ => Sync[F].pure(()))
    } yield git
}
