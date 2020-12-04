package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Categories
import au.id.smw.shoppingcart.models._
import cats.{ Defer, MonadError }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }

final class AdminCategoryRoutes[F[_]: MonadError[*[_], Throwable]: Defer: JsonDecoder](
    categories: Categories[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
    case authedReq @ POST -> Root as _ =>
      authedReq.req.decodeR[CategoryParam] { category =>
        Created(categories.create(category.toDomain))
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
