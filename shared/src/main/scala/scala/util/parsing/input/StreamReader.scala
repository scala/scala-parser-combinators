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

/** An object to create a `StreamReader` from a `java.io.Reader`.
 */
object StreamReader {
  final val EofCh = '\u001a'

  /** Create a `StreamReader` from a `java.io.Reader`.
   *
   * @param in the `java.io.Reader` that provides the underlying
   *           stream of characters for this Reader.
   */
  def apply(in: java.io.Reader): StreamReader = {
    new StreamReader(PagedSeq.fromReader(in), 0, 1)
  }
}

/** A StreamReader reads from a character sequence, typically created as a PagedSeq
 *  from a java.io.Reader
 *
 *  NOTE:
 *  StreamReaders do not really fulfill the new contract for readers, which
 *  requires a `source` CharSequence representing the full input.
 *  Instead source is treated line by line.
 *  As a consequence, regex matching cannot extend beyond a single line
 *  when a StreamReader are used for input.
 *
 *  If you need to match regexes spanning several lines you should consider
 *  class `PagedSeqReader` instead.
 */
sealed class StreamReader private (seq: PagedSeq[Char], off: Int, lnum: Int, nextEol0: Int) extends PagedSeqReader(seq, off) {
  def this(seq: PagedSeq[Char], off: Int, lnum: Int) = this(seq, off, lnum, -1)

  import StreamReader.EofCh

  override def rest: StreamReader =
    if (!seq.isDefinedAt(off)) this
    else if (seq(off) == '\n')
      new StreamReader(seq.slice(off + 1), 0, lnum + 1, -1)
    else new StreamReader(seq, off + 1, lnum, nextEol0)

  private def nextEol = if (nextEol0 == -1) {
    var i = off
    while (seq.isDefinedAt(i) && seq(i) != '\n' && seq(i) != EofCh) i += 1
    i
  } else nextEol0

  override def drop(n: Int): StreamReader = {
    val eolPos = nextEol
    if (eolPos < off + n && seq.isDefinedAt(eolPos))
      new StreamReader(seq.slice(eolPos + 1), 0, lnum + 1, -1).drop(off + n - (eolPos + 1))
    else
      new StreamReader(seq, off + n, lnum, eolPos)
  }

  override def pos: Position = new Position {
    def line = lnum
    def column = off + 1
    def lineContents = seq.slice(0, nextEol).toString
  }
}
