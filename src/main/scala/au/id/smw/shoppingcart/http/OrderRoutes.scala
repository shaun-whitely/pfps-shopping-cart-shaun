package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Orders
import au.id.smw.shoppingcart.models._
import cats.{ Defer, Monad }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }

final class OrderRoutes[F[_]: Monad: Defer](
    orders: Orders[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user                 => Ok(orders.findByUser(user.value.id))
    case GET -> Root / UUIDVar(uuid) as user => Ok(orders.get(user.value.id, OrderId(uuid)))
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
