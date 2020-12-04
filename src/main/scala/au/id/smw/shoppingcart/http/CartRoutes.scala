package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.ShoppingCart
import au.id.smw.shoppingcart.models._
import cats.implicits._
import cats.{ Defer, Monad }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }

final class CartRoutes[F[_]: Monad: Defer: JsonDecoder](
    shoppingCart: ShoppingCart[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/cart"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user => Ok(shoppingCart.get(user.value.id))
    case authedReq @ POST -> Root as user =>
      authedReq.req
        .asJsonDecode[Cart]
        .flatMap(
          _.items
            .map {
              case (item, quantity) => shoppingCart.addItem(user.value.id, item, quantity)
            }
            .toList
            .sequence
            .productR(Created())
        )

    case authedReq @ PUT -> Root as user =>
      authedReq.req
        .asJsonDecode[Cart]
        .flatMap(cart => shoppingCart.update(user.value.id, cart))
        .productR(Ok())
    case DELETE -> Root / UUIDVar(uuid) as user =>
      shoppingCart
        .removeItem(user.value.id, ItemId(uuid))
        .productR(NoContent())
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
