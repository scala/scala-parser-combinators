/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.{Assert, Test}

import scala.util.parsing.combinator.RegexParsers

class CompletionForIntoTest {
  val animal  = "animal"
  val machine = "machine"
  val bear    = "bear"
  val lion    = "lion"

  object TestParser extends RegexParsers with RegexCompletionSupport {
    val animalParser  = bear | lion
    val machineParser = "plane" | "car"
    val test = (animal | machine) >> { kind: String =>
      if (kind == animal) animalParser else machineParser
    }
  }

  @Test
  def intoParserWithoutSuccessCompletesToParser(): Unit = {
    val completions = TestParser.completeString(TestParser.test, "")
    Assert.assertEquals(Seq(animal, machine), completions)
  }

  @Test
  def intoParserWithSuccessCompletesResultingParser(): Unit = {
    val completions = TestParser.completeString(TestParser.test, animal)
    Assert.assertEquals(Seq(bear, lion), completions)
  }
}
