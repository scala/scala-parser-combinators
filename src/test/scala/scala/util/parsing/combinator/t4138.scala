import org.junit.Test
import org.junit.Assert.assertEquals

class T4138 {
  object p extends scala.util.parsing.combinator.JavaTokenParsers

  @Test
  def test: Unit = {
    assertEquals("""[1.45] parsed: "lir 'de\' ' \\ \n / upa \"new\" \t parsing"""", p.parse(p.stringLiteral, """"lir 'de\' ' \\ \n / upa \"new\" \t parsing"""").toString)
    assertEquals("""[1.5] parsed: "s """", p.parse(p.stringLiteral, """"s " lkjse"""").toString)
  }
}
