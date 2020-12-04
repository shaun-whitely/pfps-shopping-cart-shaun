package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.models._
import au.id.smw.shoppingcart.algebras.Categories
import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class CategoryRoutes[F[_]: Monad: Defer](categories: Categories[F]) extends Http4sDsl[F] {
  private val prefixPath = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root => Ok(categories.findAll)
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}
