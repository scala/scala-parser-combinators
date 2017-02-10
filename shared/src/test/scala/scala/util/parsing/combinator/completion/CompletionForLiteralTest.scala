/*                                                      *\
**  scala-parser-combinators completion fork            **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
**  Author: jonas.chapuis@nexthink.com                  **
\*                                                      */
package scala.util.parsing.combinator.completion

import org.junit.Assert._
import org.junit.Test

import scala.util.parsing.combinator.Parsers

class CompletionForLiteralTest {
  val someLiteral = "literal"
  val otherLiteralWithSamePrefix = "litOther"
  val someLiteralPrefix = "lit"

  object Parser extends Parsers with RegexCompletionSupport {
    val literal: Parser[String] = someLiteral

    val combination = someLiteral | otherLiteralWithSamePrefix
  }

  @Test
  def prefix_completes_toLiteral = {
    val completion = Parser.complete(Parser.literal, " " + someLiteralPrefix)
    assertEquals(2, completion.position.column)
    assertEquals(Seq(someLiteral), completion.completionStrings)
  }

  @Test
  def prefix_combinationCompletes_toBothAlternatives = {
    val completion =
      Parser.completeString(Parser.combination, someLiteralPrefix)
    assertEquals(Seq(otherLiteralWithSamePrefix, someLiteral), completion)
  }

  @Test
  def partialOther_completes_toOther = {
    val completion = Parser.completeString(
      Parser.combination,
      someLiteralPrefix + otherLiteralWithSamePrefix
        .stripPrefix(someLiteralPrefix)
        .head)
    assertEquals(Seq(otherLiteralWithSamePrefix), completion)
  }

  @Test
  def whitespace_completes_toLiteral = {
    val completion =
      Parser.complete(Parser.literal, List.fill(2)(" ").mkString)
    assertEquals(3, completion.position.column)
    assertEquals(Seq(someLiteral), completion.completionStrings)
  }

  @Test
  def empty_completes_toLiteral = {
    val completion = Parser.complete(Parser.literal, "")
    assertEquals(1, completion.position.column)
    assertEquals(Seq(someLiteral), completion.completionStrings)
  }

  @Test
  def other_completes_toNothing =
    assertEquals(
      Map(),
      Parser.complete(Parser.literal, otherLiteralWithSamePrefix).sets)

  @Test
  def completeLiteral_completes_toEmpty =
    assertTrue(Parser.complete(Parser.literal, someLiteral).sets.isEmpty)

}
