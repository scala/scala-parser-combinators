package scala.util.parsing.combinator

import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

/**
  * Test for issue 56: https://github.com/scala/scala-parser-combinators/issues/56
  *
  * Makes sure that lineContents (and thus longString) in the Position trait doesn't
  * include a newline
  */
class t56 {
  private object grammar extends StandardTokenParsers with PackratParsers {
    lazy val term = (numericLit | stringLit | ident)+
  }

  @Test
  def test1: Unit = {
    import grammar._

    val expr =
      """/* an unclosed comment
        |of multiple lines
        |just to check longString/lineContents
      """.stripMargin

    val fail =
      """[1.1] failure: identifier expected
        |
        |/* an unclosed comment
        |^""".stripMargin

    val parseResult = phrase(term)(new lexical.Scanner(expr))
    assertTrue(parseResult.isInstanceOf[Failure])
    assertEquals(fail, parseResult.toString)
  }


  @Test
  def test2: Unit = {
    import grammar._

    val expr = "/* an unclosed comment without newline"

    val fail =
      """[1.1] failure: identifier expected
        |
        |/* an unclosed comment without newline
        |^""".stripMargin

    val parseResult = phrase(term)(new lexical.Scanner(expr))
    assertTrue(parseResult.isInstanceOf[Failure])
    assertEquals(fail, parseResult.toString)
  }
}
