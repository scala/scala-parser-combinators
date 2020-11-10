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

/** This component provides the notion of `Token`, the unit of information that is passed from lexical
 * parsers in the `Lexical` component to the parsers in the `TokenParsers` component.
 */
trait Tokens {
  /** Objects of this type are produced by a lexical parser or ``scanner``, and consumed by a parser.
   *
   *  @see [[scala.util.parsing.combinator.syntactical.TokenParsers]]
   */
  abstract class Token {
    def chars: String
  }

  /** A class of error tokens. Error tokens are used to communicate
   *  errors detected during lexical analysis
   */
  case class ErrorToken(msg: String) extends Token {
    def chars = s"*** error: $msg"
  }

  /** A class for end-of-file tokens */
  case object EOF extends Token {
    def chars = "<eof>"
  }

  /** This token is produced by a scanner `Scanner` when scanning failed. */
  def errorToken(msg: String): Token = ErrorToken(msg)
}
