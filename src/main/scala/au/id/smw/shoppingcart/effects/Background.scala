package au.id.smw.shoppingcart.effects

import cats.effect.implicits._
import cats.effect.{Concurrent, Timer}
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

trait Background[F[_]] {
  def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit]
}

object Background {
  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev

  implicit def concurrentBackground[F[_]: Concurrent: Timer]: Background[F] = new Background[F] {
    override def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
      (Timer[F].sleep(duration) *> fa).start.void
  }
}
