/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.Assert._
import org.junit.Test

import scala.util.parsing.input.NoPosition

class CompletionTypesTest extends CompletionTypes {
  override type Elem = Char

  val setA = CompletionSet(CompletionTag("A", 10), Set(Completion("a", 2), Completion("b", 1)))
  val setB = CompletionSet(CompletionTag("B", 5), Set(Completion("c", 4), Completion("d", 3)))
  val setC = CompletionSet("C", Completion("e", 10))

  @Test
  def completionsTakeTopWorks() = {
    // Arrange
    val compl = Completions(Seq(setA, setB, setC))

    // Act
    val lettersInOrder = Seq("a", "b", "c", "d", "e")
    val letterSets     = for (i <- 1 until lettersInOrder.length) yield lettersInOrder.take(i)
    letterSets.foreach(set => assertEquals(set, compl.takeTop(set.length).completionStrings))
  }

  @Test
  def completionsSetsScoredWithMaxCompletionWorks() = {
    // Arrange
    val compl = Completions(Seq(setA, setB, setC))

    // Act
    assertEquals(Seq("e", "c", "d", "a", "b"), compl.setsScoredWithMaxCompletion().completionStrings)
  }
}
