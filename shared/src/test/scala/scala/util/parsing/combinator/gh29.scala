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

import org.junit.Test
import org.junit.Assert.assertEquals

class gh29 {
  object Foo extends JavaTokenParsers {
    def word(x: String) = s"\\b$x\\b".r

    lazy val expr  = aSentence | something

    lazy val aSentence = noun ~ verb ~ obj

    lazy val noun   = word("noun")
    lazy val verb   = word("verb") | err("not a verb!")
    lazy val obj    = word("object")

    lazy val something = word("FOO")
  }

  val expected =
    """[1.6] error: not a verb!

noun vedsfasdf
     ^""".stripMargin

  @Test
  def test(): Unit = {
    val f = Foo.parseAll(Foo.expr, "noun verb object")

    assertEquals("[1.17] parsed: ((noun~verb)~object)", f.toString)

    val g = Foo.parseAll(Foo.expr, "noun vedsfasdf")
    assertEquals(expected, g.toString)
  }
}
