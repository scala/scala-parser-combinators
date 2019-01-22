/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util.parsing.input

private[input] trait ScalaVersionSpecificPagedSeq[T] { self: PagedSeq[T] =>
  // Members declared in scala.collection.Seq
  override def iterableFactory: collection.SeqFactory[collection.IndexedSeq] = collection.IndexedSeq

}
