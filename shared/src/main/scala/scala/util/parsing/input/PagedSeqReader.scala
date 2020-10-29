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

package scala
package util.parsing.input

/** An object encapsulating basic character constants.
 *
 * @author Martin Odersky
 * @author Adriaan Moors
 */
object PagedSeqReader {
  final val EofCh = '\u001a'
}

/** A character array reader reads a stream of characters (keeping track of their positions)
 * from an array.
 *
 * @param seq     the source sequence
 * @param offset  starting offset.
 *
 * @author Martin Odersky
 */
class PagedSeqReader(seq: PagedSeq[Char],
                     override val offset: Int) extends Reader[Char] { outer =>
  import PagedSeqReader._

  override val source: java.lang.CharSequence = new SeqCharSequence(seq)

  /** Construct a `PagedSeqReader` with its first element at
   *  `source(0)` and position `(1,1)`.
   */
  def this(seq: PagedSeq[Char]) = this(seq, 0)

  /** Returns the first element of the reader, or EofCh if reader is at its end
   */
  def first =
    if (seq.isDefinedAt(offset)) seq(offset) else EofCh

  /** Returns a PagedSeqReader consisting of all elements except the first
   *
   * @return If `atEnd` is `true`, the result will be `this`;
   *         otherwise, it's a `PagedSeqReader` containing the rest of input.
   */
  def rest: PagedSeqReader =
    if (seq.isDefinedAt(offset)) new PagedSeqReader(seq, offset + 1) {
      override val source: java.lang.CharSequence = outer.source
    }
    else this

  /** The position of the first element in the reader.
   */
  def pos: Position = new OffsetPosition(source, offset)

  /** true iff there are no more elements in this reader (except for trailing
   *  EofCh's).
   */
  def atEnd = !seq.isDefinedAt(offset)

  /** Returns an abstract reader consisting of all elements except the first
   *  `n` elements.
   */
  override def drop(n: Int): PagedSeqReader =
    new PagedSeqReader(seq, offset + n) {
      override val source: java.lang.CharSequence = outer.source
    }
}
