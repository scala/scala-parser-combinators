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
package util.parsing
package combinator
package token

/** This component provides the standard `Token`s for a simple, Scala-like language.
 */
trait StdTokens extends Tokens {
  /** The class of keyword tokens */
  case class Keyword(chars: String) extends Token {
    override def toString = s"'$chars'"
  }

  /** The class of numeric literal tokens */
  case class NumericLit(chars: String) extends Token {
    override def toString = chars
  }

  /** The class of string literal tokens */
  case class StringLit(chars: String) extends Token {
    override def toString = s""""$chars""""
  }

  /** The class of identifier tokens */
  case class Identifier(chars: String) extends Token {
    override def toString = s"identifier $chars"
  }
}
