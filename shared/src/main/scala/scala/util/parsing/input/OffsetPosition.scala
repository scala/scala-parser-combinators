/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package util.parsing.input

import scala.collection.mutable.ArrayBuffer
import java.lang.{CharSequence, ThreadLocal}
import java.util.WeakHashMap

/** `OffsetPosition` is a standard class for positions
 *   represented as offsets into a source ``document''.
 *
 *   @param source   The source document
 *   @param offset   The offset indicating the position
 *
 * @author Martin Odersky
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
      if (source.charAt(i) == '\n') lineStarts += (i + 1)
    lineStarts += source.length
    lineStarts.toArray
  }

  /** The line number referred to by the position; line numbers start at 1. */
  def line: Int = {
    var lo = 0
    var hi = index.length - 1
    while (lo + 1 < hi) {
      val mid = (hi + lo) / 2
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
    val endIndex = if (source.charAt(index(line) - 1) == '\n') {
      index(line) -  1
    } else {
      index(line)
    }
    source.subSequence(index(line - 1), endIndex).toString
  }

  /** Returns a string representation of the `Position`, of the form `line.column`. */
  override def toString = line+"."+column

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
 *
 * @author Tomáš Janoušek
 */
object OffsetPosition extends scala.runtime.AbstractFunction2[CharSequence,Int,OffsetPosition] {
  private lazy val indexCacheTL =
    // not DynamicVariable as that would share the map from parent to child :-(
    new ThreadLocal[java.util.Map[CharSequence, Array[Int]]] {
      override def initialValue = new WeakHashMap[CharSequence, Array[Int]]
    }

  private def indexCache = indexCacheTL.get
}
