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

import scala.util.parsing.input.OffsetPosition

import org.junit.Test
import org.junit.Assert.assertEquals

class t5669 {
  @Test
  def test: Unit = {
    val op = new OffsetPosition("foo\rbar", 4)
    assertEquals(2, op.line)
  }
}
