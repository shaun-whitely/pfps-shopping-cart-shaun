package au.id.smw.shoppingcart

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    IO(println("Hello, world!")).map(_ => ExitCode.Success)
  }
}
