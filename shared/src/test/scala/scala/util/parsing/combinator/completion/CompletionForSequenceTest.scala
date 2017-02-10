/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

class CompletionForSequenceTest {
  val left = "left"
  val foo = "foo"
  val bar = "bar"
  val as = "as"
  val df = "df"

  object TestParser extends RegexCompletionParsers {
    val sequence = left ~> (foo | bar)

    val subSeqLeft = foo | foo ~ bar
    val subSeqRight = as ~ df | df ~ as
    val composedSequence = subSeqLeft ~ subSeqRight
  }

  @Test
  def empty_completes_toLeft =
    Assert.assertEquals(Seq(left), TestParser.completeString(TestParser.sequence, ""))

  @Test
  def partialLeft_completes_toLeft =
    Assert.assertEquals(Seq(left), TestParser.completeString(TestParser.sequence, left.dropRight(2)))

  @Test
  def completeLeft_completes_toRightAlternatives = {
    val completion = TestParser.complete(TestParser.sequence, left)
    Assert.assertEquals(left.length + 1, completion.position.column)
    Assert.assertEquals(Seq(bar, foo), completion.completionStrings)
  }

  @Test
  def completeLeftAndRight_completes_toNothing =
    Assert.assertEquals(Nil, TestParser.completeString(TestParser.sequence, left + "  " + bar))


  @Test
  def empty_composedCompletes_toLeft =
    Assert.assertEquals(Seq(foo), TestParser.completeString(TestParser.composedSequence, ""))

  @Test
  def left_composedCompletes_toLeftRemainingAlternativeAndRight =
    Assert.assertEquals(Seq(as, bar, df), TestParser.completeString(TestParser.composedSequence, foo))

  @Test
  def completeLeft_composedCompletes_ToCorrectRightAlternative =
    Assert.assertEquals(Seq(df), TestParser.completeString(TestParser.composedSequence, foo + " "+ as))
}
