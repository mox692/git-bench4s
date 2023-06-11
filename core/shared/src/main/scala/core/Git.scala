package core

import cats.effect.Resource
import cats.MonadError
import cats.syntax.all._
import fs2.io.process._

class Git[F[_]] {
  def lastCommit(): F[String] = ???

  def checkout(): F[Unit] = ???

  def checkoutInOtherProcess(process: Process[F]): F[Unit] = ???
}

object Git {
  private def isInitialized[F[_]](implicit F: MonadError[F, Throwable]): F[Boolean] =
    // check
    MonadError[F, Throwable].pure(true)

  def mkGit[F[_]](implicit F: MonadError[F, Throwable]): Resource[F, Git[F]] =
    for {
      git <- Resource.make {
        isInitialized[F].ifM(
          MonadError[F, Throwable].pure(new Git[F] {}),
          MonadError[F, Throwable].raiseError(
            new RuntimeException("git is not initialized.")
          )
        )
      }(_ => MonadError[F, Throwable].pure(()))
    } yield git
}
