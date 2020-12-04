package au.id.smw.shoppingcart.programs

import au.id.smw.shoppingcart.algebras.{ Orders, ShoppingCart }
import au.id.smw.shoppingcart.http.clients.PaymentsClient
import au.id.smw.shoppingcart.effects.Background
import au.id.smw.shoppingcart.models.{
  Card,
  CartItem,
  EmptyCartError,
  OrderError,
  OrderId,
  Payment,
  PaymentError,
  PaymentId,
  UserId
}
import cats.MonadError
import cats.effect.Timer
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import retry.{ retryingOnAllErrors, RetryDetails, RetryPolicy }
import squants.market.Money

import scala.annotation.unused
import scala.concurrent.duration._

final class CheckoutProgram[F[_]: MonadError[*[_], Throwable]: Logger: Timer: Background](
    paymentsClient: PaymentsClient[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F],
    retryPolicy: RetryPolicy[F]
) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart <- shoppingCart.get(userId).ensure(EmptyCartError)(_.items.nonEmpty)
      paymentId <- processPayment(Payment(userId, cart.total, card))
      orderId <- createOrder(userId, paymentId, cart.items, cart.total)
      _ <- shoppingCart.delete(userId).attempt.void
    } yield orderId

  private def processPayment(payment: Payment): F[PaymentId] = {
    val effect = retryingOnAllErrors[PaymentId](
      retryPolicy,
      logError("Payments")
    )(paymentsClient.process(payment))

    effect.adaptError {
      case e => PaymentError.causedBy(e)
    }
  }

  private def createOrder(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): F[OrderId] = {
    val createOrder = orders.create(userId, paymentId, items, total)

    val effect = retryingOnAllErrors[OrderId](
      retryPolicy,
      logError("Order")
    )(createOrder)

    def bgAction(fa: F[OrderId]): F[OrderId] =
      effect
        .adaptError {
          case e => OrderError.causedBy(e)
        }
        .onError {
          case _ =>
            Logger[F]
              .error(s"Failed to create order for payment ${paymentId}. Will try again in 1 hour.")
              .productR(Background[F].schedule(bgAction(fa), 1.hour))
        }

    bgAction(effect)
  }

  private def logError(action: String)(@unused e: Throwable, retryDetails: RetryDetails): F[Unit] =
    retryDetails match {
      case RetryDetails.GivingUp(totalRetries, _) =>
        Logger[F].info(s"Giving up on $action after $totalRetries attempts.")
      case RetryDetails.WillDelayAndRetry(_, retriesSoFar, _) =>
        Logger[F].info(s"Failed on $action. $retriesSoFar retries so far.")
    }
}
