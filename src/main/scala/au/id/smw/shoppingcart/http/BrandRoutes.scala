package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Brands
import au.id.smw.shoppingcart.models._
import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class BrandRoutes[F[_]: Monad: Defer](brands: Brands[F]) extends Http4sDsl[F] {
  private val prefixPath = "/brands"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root => Ok(brands.findAll)
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}
