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

import scala.util.parsing.combinator._

import org.junit.Test
import org.junit.Assert.assertEquals

class t6067 extends RegexParsers {
  object TestParser extends RegexParsers {
    def p: TestParser.ParseResult[TestParser.~[List[String], String]] = parseAll(rep(commit("a")) ~ "b", "aaab")
  }

  val expected = """[1.4] error: 'a' expected but 'b' found

aaab
   ^"""
  @Test
  def test: Unit = {
    assertEquals(expected, TestParser.p.toString)
  }
}
