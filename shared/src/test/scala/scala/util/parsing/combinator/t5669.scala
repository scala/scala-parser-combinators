package scala.util.parsing.combinator

import scala.language.implicitConversions
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
