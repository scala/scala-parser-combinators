package scala.util.parsing.input

private[input] trait ScalaVersionSpecificPagedSeq[T] { self: PagedSeq[T] =>
  // Members declared in scala.collection.Seq
  override def iterableFactory: collection.SeqFactory[collection.IndexedSeq] = collection.IndexedSeq

}
