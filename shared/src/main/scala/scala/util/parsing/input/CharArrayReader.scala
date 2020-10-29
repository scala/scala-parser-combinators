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
object CharArrayReader {
  final val EofCh = '\u001a'
}

/** A character array reader reads a stream of characters (keeping track of their positions)
 * from an array.
 *
 * @param chars  an array of characters
 * @param index  starting offset into the array; the first element returned will be `source(index)`
 *
 * @author Martin Odersky
 * @author Adriaan Moors
 */
class CharArrayReader(chars: Array[Char], index: Int = 0)
    extends CharSequenceReader(java.nio.CharBuffer.wrap(chars), index)
