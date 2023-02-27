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

class gh72 {
  class TestParsers extends Parsers {
    type Elem = Char
    val left: Parser[String] = 'a' ~ 'b' ~ 'c' ^^^ "left" withFailureMessage "failure on left"
    val right: Parser[String] = 'a' ~ 'b' ~ 'c' ^^^ "right" withFailureMessage "failure on right"
    def p: Parser[String] = left ||| right
  }

  @Test
  def test(): Unit = {
    val tstParsers = new TestParsers
    val s = new CharSequenceReader("abc")
    assertEquals("[1.4] parsed: left", tstParsers.p(s).toString)

    val t = new CharSequenceReader("def")
    val expectedFailure = """[1.1] failure: failure on left

def
^"""
    assertEquals(expectedFailure, tstParsers.p(t).toString)
  }
}
