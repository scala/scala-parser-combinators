package scala.util.parsing.input

import org.junit.Assert.assertEquals
import org.junit.Test
import scala.language.implicitConversions

class gh178 {

  @Test
  def test: Unit = {
    val len = 100000
    val i = Iterator.fill(len)("A")
    val pagedSeq = PagedSeq.fromStrings(i)
    assertEquals(len, pagedSeq.slice(0).length)  // should not fail with StackOverflowError
  }
}
