/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.Assert._
import org.junit.Test

class RecursiveGrammarTest {
  import CompletionTestDefinitions._

  object ExprParser extends CompletionTestParser {
    val number = "[0-9]+".r %> ("1", "10", "99") % "number" %? "any number"
    lazy val expr: Parser[Int] = term ~ rep(
        (("+" | "-") % "operators" %? "arithmetic operators" % 10) ~! term ^^ {
        case "+" ~ t => t
        case "-" ~ t => -t
      }) ^^ { case t ~ r => t + r.sum }
    lazy val multiplicationDivisionOperators = ("*" | "/") % "operators" %? "arithmetic operators" % 10
    lazy val term: Parser[Int] = factor ~ rep(multiplicationDivisionOperators ~! factor) ^^ {
      case f ~ Nil => f
      case f ~ r =>
        r.foldLeft(f) {
          case (prev, "*" ~ next) => prev * next
          case (prev, "/" ~ next) => prev / next
        }
    }
    lazy val factor: Parser[Int] = number ^^ { _.toInt } | "(" ~> expr <~ ")"
  }

  @Test
  def expressionsParseCorrectly() = {
    assertEquals(1 + 2 + 3, ExprParser.parseAll(ExprParser.expr, "1+2+3").get)
    assertEquals(2 * 3, ExprParser.parseAll(ExprParser.expr, "2*3").get)
    assertEquals(10 / (3 + 2), ExprParser.parseAll(ExprParser.expr, "(5+5)/(3+2)").get)
    assertEquals(5 * 2 / 2, ExprParser.parseAll(ExprParser.expr, "(5*2/2)").get)
    assertEquals(3 - 4 - 5, ExprParser.parseAll(ExprParser.expr, "3-4-5").get)
  }

  @Test
  def emptyCompletesToNumberOrParen() =
    ExprParser.assertHasCompletions(Set(Tagged("number", Some("any number"), 0, "1", "10", "99"), Default("(")),
                                    ExprParser.complete(ExprParser.expr, ""))

  @Test
  def numberCompletesToOperators() =
    ExprParser.assertHasCompletions(Set(Tagged("operators", Some("arithmetic operators"), 10, "*", "+", "-", "/")),
                                    ExprParser.complete(ExprParser.expr, "2"))

  @Test
  def numberAndOperationCompletesToNumberOrParen() =
    ExprParser.assertHasCompletions(Set(Tagged("number", Some("any number"), 0, "1", "10", "99"), Default("(")),
                                    ExprParser.complete(ExprParser.expr, "2*"))

  @Test
  def parenCompletesToNumberAndParen() =
    ExprParser.assertHasCompletions(Set(Tagged("number", Some("any number"), 0, "1", "10", "99"), Default("(")),
                                    ExprParser.complete(ExprParser.expr, "("))

  @Test
  def recursiveParenAndNumberCompletesToOperatorsOrParen() =
    ExprParser.assertHasCompletions(
      Set(Tagged("operators", Some("arithmetic operators"), 10, "*", "+", "-", "/"), Default(")")),
      ExprParser.complete(ExprParser.expr, "(((2"))

  @Test
  def closedParentCompletesToOperators() =
    ExprParser.assertHasCompletions(Set(Tagged("operators", Some("arithmetic operators"), 10, "*", "+", "-", "/")),
                                    ExprParser.complete(ExprParser.expr, "(5*2/2)"))
}
