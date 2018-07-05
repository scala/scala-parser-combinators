package scala.util.parsing.input

import org.junit.Test
import org.junit.Assert.assertEquals

class OffsetPositionTest {
  @Test
  def lineContentsWithTrailingLF: Unit = {
    val op = new OffsetPosition("\n", 1)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithTrailingCR: Unit = {
    val op = new OffsetPosition("\r", 1)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithTrailingCRLF: Unit = {
    val op = new OffsetPosition("\r\n", 1)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithEmptySource: Unit = {
    val op = new OffsetPosition("", 0)
    assertEquals("", op.lineContents)
  }

  @Test
  def linesWithLF: Unit = {
    val op = new OffsetPosition("foo\nbar", 4)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithCR: Unit = {
    val op = new OffsetPosition("foo\rbar", 4)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithCRLF: Unit = {
    val op = new OffsetPosition("foo\r\nbar", 5)
    assertEquals(2, op.line)
  }
}
