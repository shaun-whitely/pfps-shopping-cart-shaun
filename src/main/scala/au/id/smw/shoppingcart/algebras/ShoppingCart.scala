package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{Cart, CartTotal, ItemId, Quantity, UserId}

trait ShoppingCart[F[_]] {
  def get(userId: UserId): F[CartTotal]
  def addItem(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
  def delete(userId: UserId): F[Unit]
}
