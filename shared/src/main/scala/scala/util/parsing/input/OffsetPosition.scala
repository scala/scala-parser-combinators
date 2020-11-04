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

import scala.collection.mutable.ArrayBuffer

/** `OffsetPosition` is a standard class for positions
 *   represented as offsets into a source ``document``.
 *
 *   @param source   The source document
 *   @param offset   The offset indicating the position
 */
case class OffsetPosition(source: CharSequence, offset: Int) extends Position {

  /** An index that contains all line starts, including first line, and eof. */
  private lazy val index: Array[Int] = {
    Option(OffsetPosition.indexCache.get(source)) match {
      case Some(index) => index
      case None =>
        val index = genIndex
        OffsetPosition.indexCache.put(source, index)
        index
    }
  }

  private def genIndex: Array[Int] = {
    val lineStarts = new ArrayBuffer[Int]
    lineStarts += 0
    for (i <- 0 until source.length)
      if (source.charAt(i) == '\n' ||
        (source.charAt(i) == '\r' && (i == (source.length - 1) || source.charAt(i + 1) != '\n'))) {
        lineStarts += (i + 1)
      }
    lineStarts += source.length
    lineStarts.toArray
  }

  /** The line number referred to by the position; line numbers start at 1. */
  def line: Int = {
    var lo = 0
    var hi = index.length - 1
    while (lo + 1 < hi) {
      val mid = lo + ((hi - lo) / 2)
      if (offset < index(mid)) hi = mid
      else lo = mid
    }
    lo + 1
  }

  /** The column number referred to by the position; column numbers start at 1. */
  def column: Int = offset - index(line - 1) + 1

  /** The contents of the line numbered at the current offset.
   *
   * @return the line at `offset` (not including a newline)
   */
  def lineContents: String = {
    val lineStart = index(line - 1)
    val lineEnd = index(line)
    val endIndex =
      if (lineStart < lineEnd - 1 && source.charAt(lineEnd - 2) == '\r' && source.charAt(lineEnd - 1) == '\n') {
        lineEnd - 2
      } else if (lineStart < lineEnd && (source.charAt(lineEnd - 1) == '\r' || source.charAt(lineEnd - 1) == '\n')) {
        lineEnd - 1
      } else {
        lineEnd
      }
    source.subSequence(lineStart, endIndex).toString
  }

  /** Returns a string representation of the `Position`, of the form `line.column`. */
  override def toString = s"$line.$column"

  /** Compare this position to another, by first comparing their line numbers,
   * and then -- if necessary -- using the columns to break a tie.
   *
   * @param  that a `Position` to compare to this `Position`
   * @return true if this position's line number or (in case of equal line numbers)
   *         column is smaller than the corresponding components of `that`
   */
  override def <(that: Position) = that match {
    case OffsetPosition(_, that_offset) =>
      this.offset < that_offset
    case _ =>
      this.line < that.line ||
      this.line == that.line && this.column < that.column
  }
}

/** An object holding the index cache.
 */
object OffsetPosition extends scala.runtime.AbstractFunction2[CharSequence,Int,OffsetPosition] with PositionCache
