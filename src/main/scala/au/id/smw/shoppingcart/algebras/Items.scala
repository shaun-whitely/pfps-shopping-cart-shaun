package au.id.smw.shoppingcart.algebras

import au.id.smw.shoppingcart.models.{ BrandName, CreateItem, Item, UpdateItem }

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findByBrand(brandName: BrandName): F[List[Item]]
  def create(item: CreateItem): F[Unit]
  def update(item: UpdateItem): F[Unit]
}
