/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{CharSequenceReader, OffsetPosition, Positional, Reader}

/** This component extends `RegexParsers` with completion capability. In particular,
  * it provides completions for the `literal` parser.
  * Note that completions for the `regex` parser are undefined by default and can be specified
  * with the `%>` operator.
  *
  * @author Jonas Chapuis
  */
trait RegexCompletionSupport extends RegexParsers with CompletionSupport {
  protected val areLiteralsCaseSensitive = false

  protected def dropWhiteSpace(input: Input): Input =
    input.drop(handleWhiteSpace(input.source, input.offset) - input.offset)

  protected def handleWhiteSpace(input: Input): Int =
    handleWhiteSpace(input.source, input.offset)

  protected def findMatchOffsets(s: String, in: Input): (Int, Int) = {
    val source     = in.source
    val offset     = in.offset
    val start      = handleWhiteSpace(source, offset)
    var literalPos = 0
    var sourcePos  = start
    def charsEqual(a: Char, b: Char) =
      if (areLiteralsCaseSensitive) a == b else a.toLower == b.toLower
    while (literalPos < s.length && sourcePos < source.length && charsEqual(s.charAt(literalPos),
                                                                            source.charAt(sourcePos))) {
      literalPos += 1
      sourcePos += 1
    }
    (literalPos, sourcePos)
  }

  abstract override implicit def literal(s: String): Parser[String] =
    Parser[String](
      super.literal(s),
      in => {
        lazy val literalCompletion =
          Completions(OffsetPosition(in.source, handleWhiteSpace(in)), CompletionSet(Completion(s)))
        val (literalOffset, sourceOffset) = findMatchOffsets(s, in)
        lazy val inputAtEnd               = sourceOffset == in.source.length
        literalOffset match {
          case 0 if inputAtEnd =>
            literalCompletion // whitespace, free entry possible
          case someOffset
              if inputAtEnd & someOffset > 0 & someOffset < s.length => // partially entered literal, we are at the end
            literalCompletion
          case _ => Completions.empty
        }
      }
    )

  abstract override implicit def regex(r: Regex): Parser[String] =
    Parser(super.regex(r), _ => Completions.empty)

  override def positioned[T <: Positional](p: => Parser[T]): Parser[T] = {
    lazy val q = p
    Parser[T](super.positioned(p), q.completions)
  }

  /** Returns completions for read `in` with parser `p`. */
  def complete[T](p: Parser[T], in: Reader[Char]): Completions =
    p.completions(in)

  /** Returns completions for character sequence `in` with parser `p`. */
  def complete[T](p: Parser[T], in: CharSequence): Completions =
    p.completions(new CharSequenceReader(in))

  /** Returns flattened string completions for character sequence `in` with parser `p`. */
  def completeString[T](p: Parser[T], input: String): Seq[String] =
    complete(p, input).completionStrings

}
