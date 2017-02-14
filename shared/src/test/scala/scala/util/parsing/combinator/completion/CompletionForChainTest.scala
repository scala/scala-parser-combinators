/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.RegexParsers

class CompletionForChainTest {
  val repeated  = "rep"
  val separator = ","
  object TestParser extends RegexParsers with RegexCompletionSupport {
    val chainlParser = literal(repeated) * (separator ^^ (_ => (a: String, b: String) => a))
    val chainrParser =
      chainr1(literal(repeated), separator ^^ (_ => (a: String, b: String) => a), (a: String, b: String) => a, "")
  }

  @Test
  def repeaterCompletesToParserAndSeparatorAlternatively(): Unit = chainTest(TestParser.chainlParser)

  @Test
  def chainr1CompletesToParserAndSeparatorAlternatively(): Unit =
    chainTest(TestParser.chainrParser)

  def chainTest[T](parser: TestParser.Parser[T]) = {
    val resultRep  = TestParser.completeString(parser, "")
    val resultSep  = TestParser.completeString(parser, repeated)
    val resultRep2 = TestParser.completeString(parser, s"$repeated,")
    val resultSep2 = TestParser.completeString(parser, s"$repeated,$repeated")

    // Assert
    Assert.assertEquals(resultRep.head, repeated)
    Assert.assertEquals(resultSep.head, separator)
    Assert.assertEquals(resultRep2.head, repeated)
    Assert.assertEquals(resultSep2.head, separator)
  }
}
