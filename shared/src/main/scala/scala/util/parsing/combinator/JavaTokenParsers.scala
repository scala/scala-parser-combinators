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

/** `JavaTokenParsers` differs from [[scala.util.parsing.combinator.RegexParsers]]
 *  by adding the following definitions:
 *
 *  - `ident`
 *  - `wholeNumber`
 *  - `decimalNumber`
 *  - `stringLiteral`
 *  - `floatingPointNumber`
 */
trait JavaTokenParsers extends RegexParsers {
  /** Anything that is a valid Java identifier, according to
   * <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">The Java Language Spec</a>.
   * Generally, this means a letter, followed by zero or more letters or numbers.
   */
  def ident: Parser[String] =
      "" ~> // handle whitespace
      rep1(acceptIf(Character.isJavaIdentifierStart)("identifier expected but '" + _ + "' found"),
          elem("identifier part", Character.isJavaIdentifierPart(_: Char))) ^^ (_.mkString)

  /** An integer, without sign or with a negative sign. */
  def wholeNumber: Parser[String] =
    """-?\d+""".r

  /** Number following one of these rules:
   *
   *  - An integer. For example: `13`
   *  - An integer followed by a decimal point. For example: `3.`
   *  - An integer followed by a decimal point and fractional part. For example: `3.14`
   *  - A decimal point followed by a fractional part. For example: `.1`
   */
  def decimalNumber: Parser[String] =
    """(\d+(\.\d*)?|\d*\.\d+)""".r

  /** Double quotes (`"`) enclosing a sequence of:
   *
   *  - Any character except double quotes, control characters or backslash (`\`)
   *  - A backslash followed by another backslash, a single or double quote, or one
   *    of the letters `b`, `f`, `n`, `r` or `t`
   *  - `\` followed by `u` followed by four hexadecimal digits
   */
  def stringLiteral: Parser[String] =
    ("\""+"""([^"\x00-\x1F\x7F\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*"""+"\"").r

  /** A number following the rules of `decimalNumber`, with the following
   *  optional additions:
   *
   *  - Preceded by a negative sign
   *  - Followed by `e` or `E` and an optionally signed integer
   *  - Followed by `f`, `f`, `d` or `D` (after the above rule, if both are used)
   */
  def floatingPointNumber: Parser[String] =
    """-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r
}
