/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

class CompletionForAcceptAndElemTest {

  object TestParser extends StandardTokenParsers with CompletionSupport
  import TestParser.lexical._

  @Test
  def elemCompletesToPassedCompletions(): Unit = {
    // Arrange
    val tokens = Set[Token](NumericLit("1"), NumericLit("2"), NumericLit("3"))
    val parser =
      TestParser.elem("test", _ => true, completions = tokens)

    // Act
    val result = parser.completions(new Scanner(""))

    // Assert
    Assert.assertArrayEquals(tokens.toArray[AnyRef], result.allCompletions.map(_.value.head).toArray[AnyRef])
  }

  @Test
  def acceptElemCompletesToElem(): Unit = {
    // Arrange
    val elem   = NumericLit("1")
    val parser = TestParser.elem(elem)

    // Act
    val result = parser.completions(new Scanner(""))

    // Assert
    Assert.assertEquals(elem, headToken(result.allCompletions))
  }

  @Test
  def acceptElemListCompletesToNextInList(): Unit = {
    // Arrange
    val one    = NumericLit("1")
    val two    = NumericLit("2")
    val three  = NumericLit("3")
    val seq    = List(one, two, three)
    val parser = TestParser.accept(seq)

    // Act
    val result1     = parser.completions(new Scanner(""))
    val result2     = parser.completions(new Scanner("1"))
    val result3     = parser.completions(new Scanner("1 2"))
    val emptyResult = parser.completions(new Scanner("1 2 3"))

    // Assert
    Assert.assertEquals(one, headToken(result1.allCompletions))
    Assert.assertEquals(two, headToken(result2.allCompletions))
    Assert.assertEquals(three, headToken(result3.allCompletions))
    Assert.assertTrue(emptyResult.allCompletions.isEmpty)
  }

  @Test
  def acceptWithPartialFunctionCompletesToPassedCompletions(): Unit = {
    // Arrange
    case class Number(n: Int)
    val tokens = Set[Token](NumericLit("1"), NumericLit("2"), NumericLit("3"))
    val parser = TestParser.accept("number", {case NumericLit(n) => Number(n.toInt)}, tokens)

    // Act
    val result = parser.completions(new Scanner(""))

    // Assert
    Assert.assertArrayEquals(tokens.toArray[AnyRef], result.allCompletions.map(_.value.head).toArray[AnyRef])
  }

  def headToken(completions: Iterable[TestParser.Completion]) = completions.map(_.value).head.head
}
