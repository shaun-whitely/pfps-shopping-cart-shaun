package au.id.smw.shoppingcart

import cats.{ Applicative, MonadError }
import cats.implicits._
import eu.timepit.refined.api.{ Refined, Validate }
import eu.timepit.refined.refineV
import io.circe.{ Decoder, Encoder }
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityEncoder, ParseFailure, QueryParamDecoder, Request, Response }
import org.http4s.circe._

package object http {
  implicit def coercibleQueryParamDecoder[
      A: Coercible[B, *],
      B: QueryParamDecoder
  ]: QueryParamDecoder[A] = QueryParamDecoder[B].map(_.coerce)

  implicit def refinedParamDecoder[T: QueryParamDecoder, P](
      implicit ev: Validate[T, P]
  ): QueryParamDecoder[T Refined P] =
    QueryParamDecoder[T].emap(
      refineV[P](_).leftMap(m => ParseFailure(m, m))
    )

  implicit def deriveEntityEncoder[
      F[_]: Applicative,
      A: Encoder
  ]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  implicit class RefinedRequestDecoder[F[_]: MonadError[*[_], Throwable]: JsonDecoder](
      req: Request[F]
  ) extends Http4sDsl[F] {
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") => BadRequest(c.getMessage)
            case _                                               => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }
  }
}
