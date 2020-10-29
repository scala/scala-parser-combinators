package scala.util.parsing.combinator

import scala.util.parsing.input.{CharSequenceReader, OffsetPosition}

import org.junit.Test
import org.junit.Assert.assertEquals

class t7483 {
  val s = "foo\nbar"
  val reader = new CharSequenceReader(s, 0)
  val p = reader.pos.asInstanceOf[OffsetPosition]

  @Test
  def test: Unit = {
    assertEquals("foo", p.lineContents)
  }
}
