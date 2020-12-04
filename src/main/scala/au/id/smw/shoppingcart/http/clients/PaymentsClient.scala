package au.id.smw.shoppingcart.http.clients

import au.id.smw.shoppingcart.http._
import au.id.smw.shoppingcart.models._
import cats.MonadError
import cats.implicits._
import org.http4s.Method._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{ Status, Uri }

trait PaymentsClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

final class LivePaymentsClient[F[_]: MonadError[*[_], Throwable]: JsonDecoder](
    client: Client[F]
) extends PaymentsClient[F]
    with Http4sClientDsl[F] {
  private val baseUri = "http://localhost:8080/api/v1"

  override def process(payment: Payment): F[PaymentId] =
    Uri
      .fromString(baseUri + "/payments")
      .liftTo[F]
      .flatMap { uri =>
        client.fetch[PaymentId](POST(payment, uri)) { resp =>
          resp.status match {
            case Status.Ok | Status.Conflict =>
              resp.asJsonDecode[PaymentId]
            case other =>
              PaymentError(if (other.reason.nonEmpty) other.reason else "Unknown")
                .raiseError[F, PaymentId]
          }
        }
      }
}
