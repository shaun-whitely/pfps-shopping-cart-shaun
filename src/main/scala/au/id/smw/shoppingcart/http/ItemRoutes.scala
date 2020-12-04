package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Items
import au.id.smw.shoppingcart.models._
import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class ItemRoutes[F[_]: Monad: Defer](items: Items[F]) extends Http4sDsl[F] {
  private val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(
        brand.fold(items.findAll)(b => items.findByBrand(b.toDomain))
      )
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}
