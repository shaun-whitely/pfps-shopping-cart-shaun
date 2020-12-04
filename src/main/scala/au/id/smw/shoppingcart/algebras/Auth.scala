package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{ Password, User, UserName }
import dev.profunktor.auth.jwt.JwtToken

trait Auth[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}
