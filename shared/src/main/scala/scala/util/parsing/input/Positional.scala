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

/** A trait for objects that have a source position.
 */
trait Positional {

  /** The source position of this object, initially set to undefined. */
  var pos: Position = NoPosition

  /** If current source position is undefined, update it with given position `newpos`
   *  @return  the object itself
   */
  def setPos(newpos: Position): this.type = {
    if (pos eq NoPosition) pos = newpos
    this
  }
}


