package scala.util.parsing.combinator.completion

import org.junit.Assert._
import org.junit.Test

import scala.util.parsing.input.NoPosition

/**
  * Copyright (c) by Nexthink S.A.
  * Lausanne, Switzerland (http://www.nexthink.com)
  * Created by Jonas on 09.01.2017.
  */
class CompletionDefinitionsTest extends CompletionDefinitions {
  override type Elem = Char

  val setA = CompletionSet(CompletionTag("A", 10), Set(Completion("a", 2), Completion("b", 1)))
  val setB = CompletionSet(CompletionTag("B", 5), Set(Completion("c", 4), Completion("d", 3)))
  val setC = CompletionSet("C", Completion("e", 10))

  @Test
  def completions_takeTop_works() = {
    // Arrange
    val compl = Completions(Seq(setA, setB, setC))

    // Act
    val lettersInOrder = Seq("a", "b", "c", "d", "e")
    val letterSets     = for (i <- 1 until lettersInOrder.length) yield lettersInOrder.take(i)
    letterSets.foreach(set => assertEquals(set, compl.takeTop(set.length).completionStrings))
  }

  @Test
  def completions_setsScoredWithMaxCompletion_works() = {
    // Arrange
    val compl = Completions(Seq(setA, setB, setC))

    // Act
    assertEquals(Seq("e", "c", "d", "a", "b"), compl.setsScoredWithMaxCompletion().completionStrings)
  }
}
