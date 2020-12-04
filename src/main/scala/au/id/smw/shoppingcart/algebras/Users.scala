package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{Password, User, UserId, UserName}

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}
