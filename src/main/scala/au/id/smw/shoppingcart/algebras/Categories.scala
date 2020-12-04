package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{Category, CategoryName}

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}
