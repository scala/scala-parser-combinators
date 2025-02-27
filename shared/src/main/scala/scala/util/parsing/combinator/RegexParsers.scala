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
package util.parsing.combinator

import scala.util.matching.Regex
import scala.util.parsing.input._
import scala.language.implicitConversions

/** The ''most important'' differences between `RegexParsers` and
 *  [[scala.util.parsing.combinator.Parsers]] are:
 *
 *  - `Elem` is defined to be [[scala.Char]]
 *  - There's an implicit conversion from [[java.lang.String]] to `Parser[String]`,
 *    so that string literals can be used as parser combinators.
 *  - There's an implicit conversion from [[scala.util.matching.Regex]] to `Parser[String]`,
 *    so that regex expressions can be used as parser combinators.
 *  - The parsing methods call the method `skipWhitespace` (defaults to `true`) and, if true,
 *    skip any whitespace before each parser is called.
 *  - Protected val `whiteSpace` returns a regex that identifies whitespace.
 *
 *  For example, this creates a very simple calculator receiving `String` input:
 *
 *  {{{
 *  object Calculator extends RegexParsers {
 *    def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ { _.toDouble }
 *    def factor: Parser[Double] = number | "(" ~> expr <~ ")"
 *    def term  : Parser[Double] = factor ~ rep( "*" ~ factor | "/" ~ factor) ^^ {
 *      case number ~ list => list.foldLeft(number) {
 *        case (x, "*" ~ y) => x * y
 *        case (x, "/" ~ y) => x / y
 *      }
 *    }
 *    def expr  : Parser[Double] = term ~ rep("+" ~ log(term)("Plus term") | "-" ~ log(term)("Minus term")) ^^ {
 *      case number ~ list => list.foldLeft(number) {
 *        case (x, "+" ~ y) => x + y
 *        case (x, "-" ~ y) => x - y
 *      }
 *    }
 *
 *    def apply(input: String): Double = parseAll(expr, input) match {
 *      case Success(result, _) => result
 *      case failure : NoSuccess => scala.sys.error(failure.msg)
 *    }
 *  }
 *  }}}
 */
trait RegexParsers extends Parsers {

  type Elem = Char

  protected val whiteSpace = """\s+""".r

  def skipWhitespace = whiteSpace.toString.length > 0

  /** Method called to handle whitespace before parsers.
   *
   *  It checks `skipWhitespace` and, if true, skips anything
   *  matching `whiteSpace` starting from the current offset.
   *
   *  @param source  The input being parsed.
   *  @param offset  The offset into `source` from which to match.
   *  @return        The offset to be used for the next parser.
   */
  protected def handleWhiteSpace(source: java.lang.CharSequence, offset: Int): Int =
    if (skipWhitespace)
      (whiteSpace findPrefixMatchOf (new SubSequence(source, offset))) match {
        case Some(matched) => offset + matched.end
        case None => offset
      }
    else
      offset

  /** A parser that matches a literal string */
  implicit def literal(s: String): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      var i = 0
      var j = start
      while (i < s.length && j < source.length && s.charAt(i) == source.charAt(j)) {
        i += 1
        j += 1
      }
      if (i == s.length)
        Success(source.subSequence(start, j).toString, in.drop(j - offset), None)
      else  {
        val found = if (start == source.length()) "end of source" else "'"+source.charAt(start)+"'"
        Failure("'"+s+"' expected but "+found+" found", in.drop(start - offset))
      }
    }
  }

  /** A parser that matches a regex string */
  implicit def regex(r: Regex): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      (r findPrefixMatchOf (new SubSequence(source, start))) match {
        case Some(matched) =>
          Success(source.subSequence(start, start + matched.end).toString,
                  in.drop(start + matched.end - offset),
                  None)
        case None =>
          val found = if (start == source.length()) "end of source" else "'"+source.charAt(start)+"'"
          Failure("string matching regex '"+r+"' expected but "+found+" found", in.drop(start - offset))
      }
    }
  }

  /** `positioned` decorates a parser's result with the start position of the input it consumed.
   * If whitespace is being skipped, then it is skipped before the start position is recorded.
   *
   * @param p a `Parser` whose result conforms to `Positional`.
   * @return A parser that has the same behaviour as `p`, but which marks its result with the
   *         start position of the input it consumed after whitespace has been skipped, if it
   *         didn't already have a position.
   */
  override def positioned[T <: Positional](p: => Parser[T]): Parser[T] = {
    val pp = super.positioned(p)
    new Parser[T] {
      def apply(in: Input) = {
        val offset = in.offset
        val start = handleWhiteSpace(in.source, offset)
        pp(in.drop (start - offset))
      }
    }
  }

  // we might want to make it public/protected in a future version
  private def ws[T](p: Parser[T]): Parser[T] = new Parser[T] {
    def apply(in: Input) = {
      val offset = in.offset
      val start = handleWhiteSpace(in.source, offset)
      p(in.drop (start - offset))
    }
  }

  /**
    * @inheritdoc
    *
    * This parser additionally skips whitespace if `skipWhitespace` returns true.
    */
  override def err(msg: String) = ws(super.err(msg))

  /**
   * A parser generator delimiting whole phrases (i.e. programs).
   *
   * `phrase(p)` succeeds if `p` succeeds and no input is left over after `p`.
   *
   * @param p the parser that must consume all input for the resulting parser
   *          to succeed.
   *
   * @return  a parser that has the same result as `p`, but that only succeeds
   *          if `p` consumed all the input.
   */
  override def phrase[T](p: Parser[T]): Parser[T] =
    super.phrase(p <~ "".r)

  /** Parse some prefix of reader `in` with parser `p`. */
  def parse[T](p: Parser[T], in: Reader[Char]): ParseResult[T] =
    p(in)

  /** Parse some prefix of character sequence `in` with parser `p`. */
  def parse[T](p: Parser[T], in: java.lang.CharSequence): ParseResult[T] =
    p(new CharSequenceReader(in))

  /** Parse some prefix of reader `in` with parser `p`. */
  def parse[T](p: Parser[T], in: java.io.Reader): ParseResult[T] =
    p(new PagedSeqReader(PagedSeq.fromReader(in)))

  /** Parse all of reader `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: Reader[Char]): ParseResult[T] =
    parse(phrase(p), in)

  /** Parse all of reader `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: java.io.Reader): ParseResult[T] =
    parse(phrase(p), in)

  /** Parse all of character sequence `in` with parser `p`. */
  def parseAll[T](p: Parser[T], in: java.lang.CharSequence): ParseResult[T] =
    parse(phrase(p), in)
}
