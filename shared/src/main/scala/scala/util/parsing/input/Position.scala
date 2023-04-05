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

/** `Position` is the base trait for objects describing a position in a `document`.
 *
 *  It provides functionality for:
 *   - generating a visual representation of this position (`longString`);
 *   - comparing two positions (`<`).
 *
 *  To use this class for a concrete kind of `document`, implement the `lineContents` method.
 */
trait Position {

  /** The line number referred to by the position; line numbers start at 1. */
  def line: Int

  /** The column number referred to by the position; column numbers start at 1. */
  def column: Int

  /** The contents of the line at this position. (must not contain a new-line character).
   */
  protected def lineContents: String

  /** Returns a string representation of the `Position`, of the form `line.column`. */
  override def toString = s"$line.$column"

  /** Returns a more ``visual`` representation of this position.
   *  More precisely, the resulting string consists of two lines:
   *   1. the line in the document referred to by this position
   *   2. a caret indicating the column
   *
   *  Example:
   *  {{{
   *    List(this, is, a, line, from, the, document)
   *                 ^
   *  }}}
   */
  def longString = lineContents+"\n"+lineContents.take(column-1).map{x => if (x == '\t') x else ' ' } + "^"

  /** Compare this position to another, by first comparing their line numbers,
   * and then -- if necessary -- using the columns to break a tie.
   *
   * @param `that` a `Position` to compare to this `Position`
   * @return true if this position's line number or (in case of equal line numbers)
   *         column is smaller than the corresponding components of `that`
   */
  def <(that: Position) = {
    this.line < that.line ||
    this.line == that.line && this.column < that.column
  }

  /** Compare this position to another, checking for equality.
   *
   * @param `that` a `Position` to compare to this `Position`
   * @return true if the line numbers and column numbers are equal.
   */
  override def equals(other: Any) = {
    other match {
      case that: Position => this.line == that.line && this.column == that.column
      case _ => false
    }
  }
}
