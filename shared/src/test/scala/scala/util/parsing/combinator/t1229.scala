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

import scala.util.parsing.combinator.RegexParsers

import org.junit.Test
import org.junit.Assert.assertEquals

class t1229 extends RegexParsers {
  val number = """0|[1-9]\d*""".r ^^ { _.toInt }

  val parser: Parser[Int] = number - "42"

  @Test
  def test: Unit = {
    assertEquals("[1.3] parsed: 21", parse(phrase(parser), "21").toString)

    val expected = """[1.1] failure: Expected failure

42
^"""
    assertEquals(expected, parse(phrase(parser), "42").toString )
  }
}
