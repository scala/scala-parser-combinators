package scala.util.parsing.input

import org.junit.Test
import org.junit.Assert.assertEquals

class OffsetPositionTest {
  @Test
  def printLineContentsWithTrailingNewLine: Unit = {
    val op = new OffsetPosition("\n", 1)
    assertEquals(op.lineContents, "")
  }

  @Test
  def printLineContentsWithEmptySource: Unit = {
    val op = new OffsetPosition("", 0)
    assertEquals(op.lineContents, "")
  }
}
