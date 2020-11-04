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

/** Undefined position.
 */
object NoPosition extends Position {
  def line = 0
  def column = 0
  override def toString = "<undefined position>"
  override def longString = toString
  def lineContents = ""
}
