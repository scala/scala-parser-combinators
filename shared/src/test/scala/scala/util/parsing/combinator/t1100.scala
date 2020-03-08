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

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader

import org.junit.Test
import org.junit.Assert.assertEquals

class T1100 {
  class TestParsers extends Parsers {
    type Elem = Char

    def p: Parser[List[Char]] = rep1(p1)
    def p1: Parser[Char] = accept('a') | err("errors are propagated")
  }

  val expected = """[1.4] error: errors are propagated

aaab
   ^"""

  @Test
  def test(): Unit = {
    val tstParsers = new TestParsers
    val s = new CharSequenceReader("aaab")
    assertEquals(expected, tstParsers.p(s).toString)
  }
}
