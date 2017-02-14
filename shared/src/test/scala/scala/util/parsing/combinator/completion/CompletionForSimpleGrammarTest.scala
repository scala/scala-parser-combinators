/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.Test

class CompletionForSimpleGrammarTest {
  import CompletionTestDefinitions._

  object SimpleGrammar extends CompletionTestParser {
    val number = "[0-9]+".r %> ("1", "10", "99") % "number" %? "any number"

    def expr: Parser[Int] = term | "(" ~> term <~ ")"
    def term: Parser[Int] = number ^^ {
      _.toInt
    }
  }

  @Test
  def emptyCompletesToNumberOrParen() =
    SimpleGrammar.assertHasCompletions(
      Set(Tagged("number", Some("any number"), 0, "1", "10", "99"), Default("(")),
      SimpleGrammar.complete(SimpleGrammar.expr, ""))

  @Test
  def invalidCompletesToNothing() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "invalid"))


  @Test
  def leftParenCompletesToNumber() =
    SimpleGrammar.assertHasCompletions(
      Set(Tagged("number", Some("any number"), 0, "1", "10", "99")),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
                             "("))

  @Test
  def leftParenAndNumberCompletesToRightParen() =
    SimpleGrammar.assertHasCompletions(
      Set(Default(")")),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
        "(8"))

  @Test
  def leftParenAndInvalidCompletesToNothing() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
        "(invalid"))

  @Test
  def parenNumberCompletesToEmpty() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "(56) "))

  @Test
  def numberCompletesToEmpty() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "28 "))

}
