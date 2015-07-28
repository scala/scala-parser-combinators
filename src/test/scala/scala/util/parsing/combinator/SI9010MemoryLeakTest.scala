package scala.util.parsing.combinator

import org.junit.{Test}

/**
 * Test that no references are left in LastNoSuccessHelper's DynamicVariable
 * after parsing.
 */
class SI9010MemoryLeakTest {
  
  class TestParser extends JavaTokenParsers {
    val token: Parser[String] = "a"
  }

  @Test
  def shouldNotLeaveReferencesAfterFailedParse(): Unit = {
    val testParser = new TestParser

    val parseResult = testParser.parse(testParser.token, "b")
    assert(parseResult.successful == false)
    assert(testParser.LastNoSuccessHelper.context.value == null)

    val parseAllResult = testParser.parseAll(testParser.token, "b")
    assert(parseAllResult.successful == false)
    assert(testParser.LastNoSuccessHelper.context.value == null)
  }

  @Test
  def shouldNotLeaveReferencesAfterSuccesfullParse(): Unit = {
    val testParser = new TestParser

    val parseResult = testParser.parse(testParser.token, "a")
    assert(parseResult.successful)
    assert(testParser.LastNoSuccessHelper.context.value == null)

    val parseAllResult = testParser.parseAll(testParser.token, "a")
    assert(parseAllResult.successful)
    assert(testParser.LastNoSuccessHelper.context.value == null)
  }
}
