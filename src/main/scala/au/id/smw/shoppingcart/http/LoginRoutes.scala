package au.id.smw.shoppingcart.http

import au.id.smw.shoppingcart.algebras.Auth
import au.id.smw.shoppingcart.models._
import cats.implicits._
import cats.{ Defer, MonadError }
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class LoginRoutes[F[_]: MonadError[*[_], Throwable]: Defer: JsonDecoder](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .recoverWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
