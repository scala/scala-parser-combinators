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

    def expr: CompletionParser[Int] = term | "(" ~> term <~ ")"
    def term: CompletionParser[Int] = number ^^ {
      _.toInt
    }
  }

  @Test
  def empty_completes_toNumberOrParen() =
    SimpleGrammar.assertHasCompletions(
      Set(Tagged("number", Some("any number"), 0, "1", "10", "99"), Default("(")),
      SimpleGrammar.complete(SimpleGrammar.expr, ""))

  @Test
  def invalid_completes_toNothing() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "invalid"))


  @Test
  def leftParen_completes_toNumber() =
    SimpleGrammar.assertHasCompletions(
      Set(Tagged("number", Some("any number"), 0, "1", "10", "99")),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
                             "("))

  @Test
  def leftParenAndNumber_completes_toRightParen() =
    SimpleGrammar.assertHasCompletions(
      Set(Default(")")),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
        "(8"))

  @Test
  def leftParenAndInvalid_completes_toNothing() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.log(SimpleGrammar.expr)("expr"),
        "(invalid"))

  @Test
  def parenNumber_completes_toEmpty() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "(56) "))

  @Test
  def number_completes_toEmpty() =
    SimpleGrammar.assertHasCompletions(
      Set(),
      SimpleGrammar.complete(SimpleGrammar.expr, "28 "))

}
