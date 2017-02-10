package scala.util.parsing.combinator.completion
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{CharSequenceReader, OffsetPosition, Positional, Reader}

/**
  * Created by jchapuis on 30.01.2017.
  */
trait RegexCompletionParsers extends RegexParsers with CompletionParsers {
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

  abstract override implicit def literal(s: String): CompletionParser[String] =
    CompletionParser[String](
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

  abstract override implicit def regex(r: Regex): CompletionParser[String] =
    CompletionParser(super.regex(r), _ => Completions.empty)

  override def positioned[T <: Positional](p: => CompletionParser[T]): CompletionParser[T] = {
    lazy val q = p
    CompletionParser[T](super.positioned(p), q.completions)
  }

  /** Returns completions for read `in` with parser `p`. */
  def complete[T](p: CompletionParser[T], in: Reader[Char]): Completions =
    p.completions(in)

  /** Returns completions for character sequence `in` with parser `p`. */
  def complete[T](p: CompletionParser[T], in: CharSequence): Completions =
    p.completions(new CharSequenceReader(in))

  /** Returns flattened string completions for character sequence `in` with parser `p`. */
  def completeString[T](p: CompletionParser[T], input: String): Seq[String] =
    complete(p, input).completionStrings

}
