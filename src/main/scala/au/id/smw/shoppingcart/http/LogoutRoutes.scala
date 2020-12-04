package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Auth
import au.id.smw.shoppingcart.models._
import cats.{ Defer, Monad }
import dev.profunktor.auth.AuthHeaders
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.server.{ AuthMiddleware, Router }

final class LogoutRoutes[F[_]: Monad: Defer](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/auth"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case authedReq @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(authedReq.req)
        .traverse_(t => auth.logout(t, user.value.name))
        .productR(NoContent())
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
