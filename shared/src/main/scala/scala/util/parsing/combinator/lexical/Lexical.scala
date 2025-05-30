/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala
package util.parsing
package combinator
package lexical

import token._
import input.CharArrayReader.EofCh

/** This component complements the `Scanners` component with
 *  common operations for lexical parsers.
 *
 *  Refer to [[scala.util.parsing.combinator.lexical.StdLexical]]
 *  for a concrete implementation for a simple, Scala-like language.
 */
abstract class Lexical extends Scanners with Tokens {

  /** A character-parser that matches a letter (and returns it).*/
  def letter = elem("letter", _.isLetter)

  /** A character-parser that matches a digit (and returns it).*/
  def digit = elem("digit", _.isDigit)

  /** A character-parser that matches any character except the ones given in `cs` (and returns it).*/
  def chrExcept(cs: Char*) = elem("", ch => !cs.contains(ch))

  /** A character-parser that matches a white-space character (and returns it).*/
  def whitespaceChar = elem("space char", ch => ch <= ' ' && ch != EofCh)
}
