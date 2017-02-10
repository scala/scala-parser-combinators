/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

class CompletionOperatorsTest {

  object TestParser extends RegexCompletionParsers {
    val someParser: CompletionParser[String] = "parser"
  }

  @Test
  def completion_specifiedWithBuilder_isCorrect = {
    // Arrange
    val completions: Seq[Seq[Char]] = Seq("one", "two", "three")
    val score = 10
    val description = "some description"
    val tag = "some tag"
    val kind = "some kind"

    assertCompletionsMatch(
      TestParser.someParser %> (completions: _*) % (tag, score) %? description %% kind,
      completions,
      Some(tag),
      Some(score),
      Some(description),
      Some(kind))

    assertCompletionsMatch(
      TestParser.someParser %> (completions: _*) % tag %? description % score %% kind,
      completions,
      Some(tag),
      Some(score),
      Some(description),
      Some(kind))

    assertCompletionsMatch(
      TestParser.someParser %> (completions: _*) % tag % score %? description %% kind,
      completions,
      Some(tag),
      Some(score),
      Some(description),
      Some(kind))
  }

  def assertCompletionsMatch[T](sut: TestParser.CompletionParser[T],
                                completions: Seq[Seq[Char]],
                                tag: Option[String],
                                score: Option[Int],
                                description: Option[String],
                                kind: Option[String]) = {
    // Act
    val result = TestParser.complete(sut, "")

    // Assert
    val completionSet: TestParser.CompletionSet =
      tag.flatMap(n => result.setWithTag(n)).orElse(result.defaultSet).get
    Assert.assertEquals(tag.getOrElse(""), completionSet.tag.label)
    Assert.assertEquals(score.getOrElse(0), completionSet.tag.score)
    Assert.assertEquals(description, completionSet.tag.description)
    Assert.assertEquals(kind, completionSet.tag.kind)
    Assert.assertEquals(completions.toSet, completionSet.completions.map(_.value))
  }

  @Test
  def unioning_completionSets_scoresMergedItemsOffsetBySetScore = {
    // Arrange
    val a = Seq(TestParser.Completion("one", 10), TestParser.Completion("two"))
    val b = Seq(TestParser.Completion("three", 5), TestParser.Completion("five"))
    val c = Seq(TestParser.Completion("four"))
    val sut = TestParser.someParser %> a % 10 | TestParser.someParser %> b | TestParser.someParser %> c % 3

    // Act
    val result = TestParser.complete(sut, "")

    // Assert
    Assert.assertArrayEquals(Seq("one", "two", "three", "four", "five").toArray[AnyRef], result.completionStrings.toArray[AnyRef])
  }

}
