package au.id.smw.shoppingcart

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import skunk.Codec

package object algebras {
  implicit class CodecOps[A](codec: Codec[A]) {
    def cimap[B: Coercible[A, *]]: Codec[B] =
      codec.imap(_.coerce[B])(_.repr.asInstanceOf[A])
  }
}
