/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import org.junit.Assert._

import scala.util.parsing.combinator.Parsers

object CompletionTestDefinitions {
  trait AssertionSet{
    def tag: String
  }
  case class Default(strings: String*) extends AssertionSet {
    def tag: String = ""
  }
  case class Tagged(tag: String, desc: Option[String], score: Int, strings: String*)
      extends AssertionSet
  case object Tagged {
    def apply(name: String, strings: String*): Tagged =
      Tagged(name, None, 0, strings: _*)
  }
}

trait CompletionTestParser extends Parsers with RegexCompletionSupport {
  import CompletionTestDefinitions._
  def assertSetEquals(expected: AssertionSet, actual: CompletionSet): Unit =
    expected match {
      case default @ Default(_ *) => {
        default.strings.zip(actual.completionStrings).foreach {
          case (expected, actual) => assertEquals(expected, actual)
        }
      }
      case named @ Tagged(name, desc, score, _ *) => {
        assertEquals(name, actual.tag.label)
        assertEquals(score, actual.tag.score)
        assertEquals(desc, actual.tag.description)
        named.strings.zip(actual.completionStrings).foreach {
          case (expected, actual) => assertEquals(expected, actual)
        }
      }
    }
  def assertHasCompletions(expected: Set[AssertionSet],
                           actual: Completions) = {
    expected.toList.sortBy(_.tag).zip(actual.allSets.toList.sortBy(_.tag.label)).foreach{
      case (expected, actual) => assertSetEquals(expected, actual)
    }
  }
}
