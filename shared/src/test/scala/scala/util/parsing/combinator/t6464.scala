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

import scala.util.parsing.input.CharSequenceReader
import scala.util.parsing.combinator.RegexParsers

import org.junit.Test
import org.junit.Assert.assertEquals

class t6464 {
  object SspParser extends RegexParsers {
    val ok: Parser[Any] =
      ("<%" ~! rep(' ') ~ "\\w+".r ~ rep(' ') ~ "%>"
        | "<%" ~! err("should not fail here, because of ~!"))

    val buggy: Parser[Any] =
      ("<%" ~! rep(' ') ~> "\\w+".r <~ rep(' ') ~ "%>"
        | "<%" ~! err("should not fail here, because of ~!"))

  }

  @Test
  def test: Unit = {
    assertEquals(
      "[1.9] parsed: ((((<%~List( ))~hi)~List( ))~%>)",
      SspParser.phrase(SspParser.ok)(new CharSequenceReader("<% hi %>")).toString)

    val expected = """[1.7] error: string matching regex '\w+' expected but '%' found

<%    %>
      ^"""

    assertEquals(
      expected,
      SspParser.phrase(SspParser.ok)(new CharSequenceReader("<%    %>")).toString)

    assertEquals(
      "[1.9] parsed: hi",
      SspParser.phrase(SspParser.buggy)(new CharSequenceReader("<% hi %>")).toString)

    assertEquals(
      expected,
      SspParser.phrase(SspParser.buggy)(new CharSequenceReader("<%    %>")).toString)
  }
}
