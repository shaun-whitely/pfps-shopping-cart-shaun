package au.id.smw.shoppingcart.http

import java.util.UUID

import au.id.smw.shoppingcart.models._
import cats.MonadError
import cats.implicits._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.{ JwtAuth, JwtToken }
import org.http4s.server.AuthMiddleware
import pdi.jwt.{ JwtAlgorithm, JwtClaim }

final class ShopAuthMiddleware[F[_]: MonadError[*[_], Throwable]] {
  private val jwtAuth = JwtAuth.hmac("53cr3t", JwtAlgorithm.HS256)

  private val usersAuth: JwtToken => JwtClaim => F[Option[User]] =
    _ => _ => User(UserId(new UUID(0, 0)), UserName("Foo")).some.pure[F]

  val usersMiddleware: AuthMiddleware[F, User] = JwtAuthMiddleware[F, User](jwtAuth, usersAuth)
}
