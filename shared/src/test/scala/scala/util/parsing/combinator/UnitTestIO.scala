import org.junit.Test
import org.junit.Assert.assertEquals

class UnitTestIO {

  @Test
  def testUTF8 {
    def decode(ch: Int) = new String(Array(ch), 0, 1).getBytes("UTF-8")
    
    assert(new String( decode(0x004D), "utf8") == new String(Array(0x004D.asInstanceOf[Char])))
    assert(new String( decode(0x0430), "utf8") == new String(Array(0x0430.asInstanceOf[Char])))
    assert(new String( decode(0x4E8C), "utf8") == new String(Array(0x4E8C.asInstanceOf[Char])))
    assert(new String(decode(0x10302), "utf8") == new String(Array(0xD800.asInstanceOf[Char],
                                                                     0xDF02.asInstanceOf[Char])))
    // a client
    val test = "{\"a\":\"\\u0022\"}"
    val expected = "a" -> "\""

    val parsed = scala.util.parsing.json.JSON.parseFull(test)
    val result = parsed == Some(Map(expected))
    assertEquals(Some(Map(expected)), parsed)
  }

  @Test
  def testSource {
    val s = "Here is a test string"
    val f = io.Source.fromBytes(s.getBytes("utf-8"))
    val b = new collection.mutable.ArrayBuffer[Char]()
    f.copyToBuffer(b)
    assertEquals(new String(b.toArray), s)
  }
}