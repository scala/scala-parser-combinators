/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.Parsers

class CompletionForRepetitionTest {
  val repeated  = "repeated"
  val separator = "separator"
  val n         = 5

  object TestParser extends Parsers with RegexCompletionSupport {
    val repSequence    = rep(repeated)
    val repSepSequence = repsep(repeated, separator)
    val error          = repsep(repeated, err("some error"))
    val repNSequence   = repN(5, repeated)

    val subSeqLeft       = "foo" ~ "bar" | "foo"
    val subSeqRight      = "as" ~ "df" | "df" ~ "as"
    val composedSequence = subSeqLeft ~ subSeqRight
    val repAlternatives  = rep1sep("foo" | composedSequence, "and")
    val repNAlternatives = repN(5, "foo" | composedSequence)
  }

  @Test
  def emptyRepCompletesToRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSequence, ""))

  @Test
  def nInstancesAndPartialRepCompletesToRepeated =
    Assert.assertEquals(
      Seq(repeated),
      TestParser.completeString(TestParser.repSequence, List.fill(3)(repeated).mkString + repeated.dropRight(3)))

  @Test
  def nInstancesOfRepeatedRepNCompletesToRepeated =
    Assert.assertEquals(Seq(repeated),
                        TestParser.completeString(TestParser.repNSequence, List.fill(3)(repeated).mkString))

  @Test
  def nInstancesPartialCompleteRepNCompletesToRepeated =
    Assert.assertEquals(
      Seq(repeated),
      TestParser.completeString(TestParser.repNSequence, List.fill(3)(repeated).mkString + repeated.dropRight(3)))

  @Test
  def nInstancesFollowedByErrorRepCompletesToNothing =
    Assert.assertEquals(Nil,
                        TestParser.completeString(TestParser.repSequence, List.fill(3)(repeated).mkString + "error"))

  @Test
  def emptyRepSepCompletesToRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSepSequence, ""))

  @Test
  def repeatedAndSeparatorRepSepCompletesToRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repSepSequence, repeated+separator))

  @Test
  def errorRepSepCompletesToNothing =
    Assert.assertEquals(Nil, TestParser.completeString(TestParser.error, repeated))

  @Test
  def emptyRepNCompletesToRepeated =
    Assert.assertEquals(Seq(repeated), TestParser.completeString(TestParser.repNSequence, ""))

  @Test
  def repAlternativesCompletesToAlternatives(): Unit =
    Assert.assertEquals(Seq("and", "as", "bar", "df"),
                        TestParser.completeString(TestParser.repAlternatives, s"foo and foo"))

  @Test
  def repNAlternativesCompletesToAlternatives(): Unit =
    Assert.assertEquals(Seq("as", "bar", "df", "foo"),
      TestParser.completeString(TestParser.repNAlternatives, s"foo foo"))
}
