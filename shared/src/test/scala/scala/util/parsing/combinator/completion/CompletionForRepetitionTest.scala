/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.Parsers

class CompletionForRepetitionTest {
  val repeated = "repeated"
  val separator = "separator"
  val n = 5

  object TestParser extends Parsers with RegexCompletionSupport {
    val repSequence = rep(repeated)
    val repSepSequence = repsep(repeated, separator)
    val error = repsep(repeated, err("some error"))
    val repNSequence = repN(5, repeated)
  }

  @Test
  def empty_repCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSequence, ""))

  @Test
  def nInstancesAndPartial_repCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSequence, List.fill(3)(repeated).mkString + repeated.dropRight(3)))

  @Test
  def nInstancesOfRepeated_repNCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repNSequence, List.fill(3)(repeated).mkString))

  @Test
  def nInstancesPartialComplete_repNCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repNSequence, List.fill(3)(repeated).mkString + repeated.dropRight(3)))

  @Test
  def nInstancesFollowedByError_repCompletes_toNothing =
    Assert.assertEquals(Nil, TestParser.completeString(TestParser.repSequence, List.fill(3)(repeated).mkString + "error"))

  @Test
  def empty_repSepCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSepSequence, ""))

  @Test
  def repeatedAndSeparator_repSepCompletes_toRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSepSequence, repeated+separator))

  @Test
  def error_repSepCompletes_ToNothing =
    Assert.assertEquals(Nil, TestParser.completeString(TestParser.error, repeated))

  @Test
  def empty_repNCompletes_ToRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repNSequence, ""))
}
