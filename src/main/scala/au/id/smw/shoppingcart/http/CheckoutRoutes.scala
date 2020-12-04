package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.models._
import au.id.smw.shoppingcart.programs.CheckoutProgram
import cats.{ Defer, MonadError }
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.circe.JsonDecoder
import org.http4s.server.{ AuthMiddleware, Router }

final class CheckoutRoutes[F[_]: MonadError[*[_], Throwable]: Defer: JsonDecoder](
    checkoutProgram: CheckoutProgram[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/checkout"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case authedReq @ POST -> Root as user =>
      authedReq.req.decodeR[Card] { card =>
        checkoutProgram
          .checkout(user.value.id, card)
          .flatMap(Created(_))
          .recoverWith {
            case EmptyCartError      => BadRequest("Cart is empty.")
            case PaymentError(cause) => BadRequest(cause)
            case OrderError(cause)   => BadRequest(cause)
          }
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
