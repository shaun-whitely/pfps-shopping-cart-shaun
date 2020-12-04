package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{ Brand, BrandId, BrandName }
import skunk.Codec
import skunk._
import skunk.implicits._
import skunk.codec.all._

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}

private object BrandQueries {
  val codec: Codec[Brand] =
    (uuid.cimap[BrandId] ~ varchar.cimap[BrandName]).imap {
      case id ~ name => Brand(id, name)
    }(b => b.id ~ b.name)

  val selectAll: Query[Void, Brand] =
    sql"SELECT * FROM brands".query(codec)

  val insertBrand: Command[Brand] =
    sql"INSERT INTO brands VALUES ($codec)".command
}
