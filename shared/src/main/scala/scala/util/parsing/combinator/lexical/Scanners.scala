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
package lexical

import input._

/** This component provides core functionality for lexical parsers.
 *
 *  See its subclasses [[scala.util.parsing.combinator.lexical.Lexical]] and -- most interestingly
 *  [[scala.util.parsing.combinator.lexical.StdLexical]], for more functionality.
 */
trait Scanners extends Parsers {
  type Elem = Char
  type Token

  /** This token is produced by a scanner `Scanner` when scanning failed. */
  def errorToken(msg: String): Token

  /** A parser that produces a token (from a stream of characters). */
  def token: Parser[Token]

  /** `Scanner` is essentially¹ a parser that produces `Token`s
   *  from a stream of characters. The tokens it produces are typically
   *  passed to parsers in `TokenParsers`.
   *
   *  @note ¹ `Scanner` is really a `Reader` of `Token`s
   */
  class Scanner(in: Reader[Char]) extends Reader[Token] {
    /** Convenience constructor (makes a character reader out of the given string) */
    def this(in: String) = this(new CharArrayReader(in.toCharArray))
    private val in1 = skip(in)
    private val (tok, rest1, rest2) = token(in1) match {
      case Success(tok, in2) => (tok, in1, in2)
      case ns: NoSuccess => (errorToken(ns.msg), ns.next, skipChar(ns.next))
    }
    private def skipChar(in: Reader[Char]) = if (in.atEnd) in else in.rest

    override def source: java.lang.CharSequence = in.source
    override def offset: Int = in.offset
    def first = tok
    def rest: Scanner = new Scanner(rest2)
    def pos = rest1.pos
    def atEnd = in.atEnd || skip(in).atEnd
  }
}
