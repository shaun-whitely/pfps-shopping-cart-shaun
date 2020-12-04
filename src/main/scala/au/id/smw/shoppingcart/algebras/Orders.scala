package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{CartItem, Order, OrderId, PaymentId, UserId}
import squants.market.Money

trait Orders[F[_]] {
  def get(userId: UserId, orderId: OrderId): F[Option[Order]]
  def findByUser(userId: UserId): F[List[Order]]
  def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): F[OrderId]
}
