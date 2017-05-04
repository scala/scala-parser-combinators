/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */
package scala.util.parsing.combinator.completion

import org.junit.Assert._
import org.junit.Test

import scala.util.parsing.combinator.Parsers

class CompletionForLongestMatchTest {
  val foo = "foo"
  val bar = "bar"

  object Parsers extends Parsers with RegexCompletionSupport {
    val samePrefix = foo ||| foo ~ bar
    val constrainedAndOpenAlternatives =  foo ~ bar ||| (".{5,}".r %> Completion("sample string longer than 5 char"))
  }

  @Test
  def normallyProblematicallyOrderedAlternativesParseCorrectly = {
    assertTrue(Parsers.parseAll(Parsers.samePrefix, foo).successful)
    assertTrue(Parsers.parseAll(Parsers.samePrefix, foo + bar).successful) // would be false with |
  }

  @Test
  def emptyCompletesToAlternatives =
    assertEquals(Seq(foo), Parsers.completeString(Parsers.samePrefix, ""))

  @Test
  def partialLongerAlternativeCompletesToLongerAlternative =
    assertEquals(Seq(bar), Parsers.completeString(Parsers.samePrefix, foo))

  @Test
  def longestParseProvidesCompletion =
    assertEquals(Seq(bar), Parsers.completeString(Parsers.constrainedAndOpenAlternatives, foo))


}
