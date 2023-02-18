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

import token._
import input.CharArrayReader.EofCh
import scala.collection.mutable

/** This component provides a standard lexical parser for a simple,
 *  [[http://scala-lang.org Scala]]-like language. It parses keywords and
 *  identifiers, numeric literals (integers), strings, and delimiters.
 *
 *  To distinguish between identifiers and keywords, it uses a set of
 *  reserved identifiers:  every string contained in `reserved` is returned
 *  as a keyword token. (Note that `=>` is hard-coded as a keyword.)
 *  Additionally, the kinds of delimiters can be specified by the
 *  `delimiters` set.
 *
 *  Usually this component is used to break character-based input into
 *  bigger tokens, which are then passed to a token-parser (see
 *  [[scala.util.parsing.combinator.syntactical.TokenParsers]].)
 */
class StdLexical extends Lexical with StdTokens {
  // see `token` in `Scanners`
  def token: Parser[Token] =
    ( identChar ~ rep( identChar | digit ) ^^ { case first ~ rest => processIdent(first :: rest mkString "") }
    | digit ~ rep( digit )                 ^^ { case first ~ rest => NumericLit(first :: rest mkString "") }
    | '\'' ~> rep( chrExcept('\'', '\n') ) >> { chars => stringEnd('\'', chars) }
    | '\"' ~> rep( chrExcept('\"', '\n') ) >> { chars => stringEnd('\"', chars) }
    | EofCh                                ^^^ EOF
    | delim
    | failure("illegal character")
    )

  /** Returns the legal identifier chars, except digits. */
  def identChar = letter | elem('_')

  /** Parses the final quote of a string literal or fails if it is unterminated. */
  private def stringEnd(quoteChar: Char, chars: List[Char]): Parser[Token] = {
    { elem(quoteChar) ^^^ StringLit(chars mkString "") } | err("unclosed string literal")
  }

  // see `whitespace in `Scanners`
  def whitespace: Parser[Any] = rep[Any](
      whitespaceChar
    | '/' ~ '*' ~ comment
    | '/' ~ '/' ~ rep( chrExcept(EofCh, '\n') )
    | '/' ~ '*' ~ rep( elem("", _ => true) ) ~> err("unclosed comment")
    )

  protected def comment: Parser[Any] = (
    rep (chrExcept (EofCh, '*')) ~ '*' ~ '/'     ^^ { _ => ' ' }
  | rep (chrExcept (EofCh, '*')) ~ '*' ~ comment ^^ { _ => ' ' }
  )

  /** The set of reserved identifiers: these will be returned as `Keyword`s. */
  val reserved = new mutable.HashSet[String]

  /** The set of delimiters (ordering does not matter). */
  val delimiters = new mutable.HashSet[String]

  protected def processIdent(name: String) =
    if (reserved contains name) Keyword(name) else Identifier(name)

  private lazy val _delim: Parser[Token] = {
    // construct parser for delimiters by |'ing together the parsers for the individual delimiters,
    // starting with the longest one -- otherwise a delimiter D will never be matched if there is
    // another delimiter that is a prefix of D
    def parseDelim(s: String): Parser[Token] = accept(s.toList) ^^ { _ => Keyword(s) }

    val d = new Array[String](delimiters.size)
    delimiters.copyToArray(d, 0)
    scala.util.Sorting.quickSort(d)
    (d.toList map parseDelim).foldRight(failure("no matching delimiter"): Parser[Token])((x, y) => y | x)
  }
  protected def delim: Parser[Token] = _delim
}
