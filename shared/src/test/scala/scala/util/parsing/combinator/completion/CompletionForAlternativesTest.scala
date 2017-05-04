/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.Parsers

class CompletionForAlternativesTest {
  val left = "left"
  val right = "right"
  val common = "common"

  object TestParser extends Parsers with RegexCompletionSupport {
    val alternativesWithCommonFirstParser = common ~ left | common ~! right
    val alternativesWithCommonPrefix = (common+left) | common ~ right
  }

  @Test
  def emptyCompletesToCommon =
    Assert.assertEquals(Seq(common), TestParser.completeString(TestParser.alternativesWithCommonFirstParser, ""))

  @Test
  def commonCompletesToLeftAndRight =
    Assert.assertEquals(Seq(left, right), TestParser.completeString(TestParser.alternativesWithCommonFirstParser, common))

  @Test
  def commonPrefixCompletesToRightSinceCompletionPositionsAreDifferent =
    Assert.assertEquals(Seq(right), TestParser.completeString(TestParser.alternativesWithCommonPrefix, common))

}
