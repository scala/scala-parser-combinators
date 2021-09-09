/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util.parsing.combinator

import scala.language.postfixOps
import scala.util.parsing.combinator.syntactical.StandardTokenParsers

import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

/**
  * Test for issue 56: https://github.com/scala/scala-parser-combinators/issues/56
  *
  * Makes sure that lineContents (and thus longString) in the Position trait doesn't
  * include a newline
  */
class gh56 {
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
        |""".stripMargin

    val fail =
      """[4.1] failure: identifier expected
        |
        |
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
      """[1.39] failure: identifier expected
        |
        |/* an unclosed comment without newline
        |                                      ^""".stripMargin

    val parseResult = phrase(term)(new lexical.Scanner(expr))
    assertTrue(parseResult.isInstanceOf[Failure])
    assertEquals(fail, parseResult.toString)
  }
}
