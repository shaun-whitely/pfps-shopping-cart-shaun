package au.id.smw.shoppingcart

import java.util.UUID

import dev.profunktor.auth.jwt.JwtToken
import eu.timepit.refined.W
import eu.timepit.refined.api.{ Refined, Validate }
import eu.timepit.refined.collection.Size
import eu.timepit.refined.string.{ MatchesRegex, Uuid, ValidBigDecimal }
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.refined._
import io.circe.{ Decoder, Encoder, KeyDecoder, KeyEncoder }
import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import squants.market.{ Money, USD }

import scala.util.control.NoStackTrace

object models {

  @newtype case class BrandId(value: UUID)
  @newtype case class BrandName(value: String)

  case class Brand(id: BrandId, name: BrandName)

  object Brand {
    implicit val decoder: Decoder[Brand] = deriveDecoder
    implicit val encoder: Encoder[Brand] = deriveEncoder
  }

  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName =
      BrandName(value.value.toLowerCase.capitalize)
  }

  object BrandParam {
    implicit val decoder: Decoder[BrandParam] = Decoder.forProduct1("name")(BrandParam.apply)
  }

  @newtype case class Quantity(value: Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])

  implicit val cartEncoder: Encoder[Cart] =
    Encoder.forProduct1("items")(_.items)

  implicit val cartDecoder: Decoder[Cart] =
    Decoder.forProduct1("items")(Cart.apply)

  @newtype case class CartId(value: UUID)

  case class CartItem(item: Item, quantity: Quantity)

  object CartItem {
    implicit val decoder: Decoder[CartItem] = deriveDecoder
    implicit val encoder: Encoder[CartItem] = deriveEncoder
  }

  case class CartTotal(items: List[CartItem], total: Money)

  object CartTotal {
    implicit val decoder: Decoder[CartTotal] = deriveDecoder
    implicit val encoder: Encoder[CartTotal] = deriveEncoder
  }

  case object EmptyCartError extends NoStackTrace

  @newtype case class CategoryId(value: UUID)
  @newtype case class CategoryName(value: String)

  case class Category(id: CategoryId, name: CategoryName)

  object Category {
    implicit val decoder: Decoder[Category] = deriveDecoder
    implicit val encoder: Encoder[Category] = deriveEncoder
  }

  @newtype case class CategoryParam(value: NonEmptyString) {
    def toDomain: CategoryName = CategoryName(value.value)
  }

  object CategoryParam {
    implicit val decoder: Decoder[CategoryParam] =
      Decoder.forProduct1("name")(CategoryParam.apply)
  }

  @newtype case class ItemId(value: UUID)
  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)

  case class Item(
      id: ItemId,
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brand: Brand,
      category: Category
  )

  object Item {
    implicit val decoder: Decoder[Item] = deriveDecoder
    implicit val encoder: Encoder[Item] = deriveEncoder
  }

  case class CreateItem(
      name: ItemName,
      description: ItemDescription,
      price: Money
      //    brandId: BrandId,
      //    categoryId: CategoryId
  )

  case class UpdateItem(
      id: ItemId,
      price: Money
  )

  @newtype case class ItemNameParam(value: NonEmptyString)
  @newtype case class ItemDescriptionParam(value: NonEmptyString)
  @newtype case class PriceParam(value: String Refined ValidBigDecimal)

  case class CreateItemParam(
      name: ItemNameParam,
      description: ItemDescriptionParam,
      price: PriceParam
  ) {
    def toDomain: CreateItem =
      CreateItem(
        ItemName(name.value.value),
        ItemDescription(description.value.value),
        USD(BigDecimal(price.value.value))
      )
  }

  object CreateItemParam {
    implicit val decoder: Decoder[CreateItemParam] = deriveDecoder
  }

  @newtype case class ItemIdParam(value: String Refined Uuid)

  case class UpdateItemParam(
      id: ItemIdParam,
      price: PriceParam
  ) {
    def toDomain: UpdateItem =
      UpdateItem(
        ItemId(UUID.fromString(id.value.value)),
        USD(BigDecimal(price.value.value))
      )
  }

  object UpdateItemParam {
    implicit val decoder: Decoder[UpdateItemParam] = deriveDecoder
  }

  @newtype case class OrderId(value: UUID)
  @newtype case class PaymentId(value: UUID)

  case class Order(
      id: OrderId,
      paymentId: PaymentId,
      items: Map[ItemId, Quantity],
      total: Money
  )

  object Order {
    implicit val decoder: Decoder[Order] = deriveDecoder
    implicit val encoder: Encoder[Order] = deriveEncoder
  }

  case class OrderError(reason: String) extends NoStackTrace

  object OrderError {
    def causedBy(e: Throwable): OrderError =
      OrderError(Option(e.getMessage).getOrElse("Unknown"))
  }

  case class Payment(id: UserId, total: Money, card: Card)

  object Payment {
    implicit val decoder: Decoder[Payment] = deriveDecoder
    implicit val encoder: Encoder[Payment] = deriveEncoder
  }

  type Rgx          = W.`"^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$"`.T
  type CardNamePred = String Refined MatchesRegex[Rgx]

  type CardNumberPred     = Long Refined Size[16]
  type CardExpirationPred = Int Refined Size[4]
  type CardCCVPred        = Int Refined Size[3]

  @newtype case class CardName(value: CardNamePred)
  @newtype case class CardNumber(value: CardNumberPred)
  @newtype case class CardExpiration(value: CardExpirationPred)
  @newtype case class CardCCV(value: CardCCVPred)

  case class Card(
      name: CardName,
      number: CardNumber,
      expiration: CardExpiration,
      ccv: CardCCV
  )

  object Card {
    implicit val decoder: Decoder[Card] = deriveDecoder
    implicit val encoder: Encoder[Card] = deriveEncoder
  }

  case class PaymentError(reason: String) extends NoStackTrace

  object PaymentError {
    def causedBy(e: Throwable): PaymentError =
      PaymentError(Option(e.getMessage).getOrElse("Unknown"))
  }

  implicit val moneyDecoder: Decoder[Money] = Decoder[BigDecimal].map(USD.apply)
  implicit val moneyEncoder: Encoder[Money] = Encoder[BigDecimal].contramap(_.amount)

  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)

  case class User(id: UserId, name: UserName)

  case class CommonUser(value: User)
  case class AdminUser(value: User)

  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class CreateUser(username: UserNameParam, password: PasswordParam)

  object CreateUser {
    implicit val decoder: Decoder[CreateUser] = deriveDecoder
    implicit val encoder: Encoder[CreateUser] = deriveEncoder
  }

  case class LoginUser(username: UserNameParam, password: PasswordParam)

  object LoginUser {
    implicit val decoder: Decoder[LoginUser] = deriveDecoder
    implicit val encoder: Encoder[LoginUser] = deriveEncoder
  }

  case class InvalidUserOrPassword(reason: String) extends NoStackTrace

  object InvalidUserOrPassword {
    def causedBy(e: Throwable): InvalidUserOrPassword =
      InvalidUserOrPassword(Option(e.getMessage).getOrElse("Unknown"))
  }

  case class UserNameInUse(userName: UserName) extends NoStackTrace

  implicit def coercibleDecoder[
      A: Coercible[B, *],
      B: Decoder
  ]: Decoder[A] = Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[
      A: Coercible[B, *],
      B: Encoder
  ]: Encoder[A] = Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit def validateSizeN[N <: Int, R](implicit w: ValueOf[N]): Validate.Plain[R, Size[N]] =
    Validate.fromPredicate[R, Size[N]](
      _.toString.length == w.value,
      _ => s"Must have ${w.value} digits",
      Size[N](w.value)
    )
}
