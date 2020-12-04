package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Items
import au.id.smw.shoppingcart.models._
import cats.{ Defer, MonadError }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }

final class AdminItemRoutes[F[_]: MonadError[*[_], Throwable]: Defer: JsonDecoder](
    items: Items[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/items"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
    case authedReq @ POST -> Root as _ =>
      authedReq.req.decodeR[CreateItemParam] { item =>
        Created(items.create(item.toDomain))
      }
    case authedReq @ PUT -> Root as _ =>
      authedReq.req.decodeR[UpdateItemParam] { item =>
        Ok(items.update(item.toDomain))
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
