package scala.util.parsing.combinator.completion

import scala.annotation.tailrec
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.Positional

/**
  * Created by jchapuis on 16.01.2017.
  */
trait CompletionParsers extends Parsers with CompletionDefinitions {
  def CompletionParser[T](f: Input => ParseResult[T], c: Input => Completions) = new CompletionParser[T] {
    def apply(in: Input)       = f(in)
    def completions(in: Input) = c(in)
  }

  implicit def parser2completion[T](p: => super.Parser[T]): CompletionParser[T] = {
    lazy val q = p
    CompletionParser(in => q(in), in => Completions.empty)
  }

  abstract class CompletionParser[+T] extends super.Parser[T] {

    def append[U >: T](q: => CompletionParser[U]): CompletionParser[U] = {
      lazy val p = q // lazy argument
      CompletionParser(
        in => super.append(q)(in),
        in => {
          val thisCompletions          = this.completions(in)
          lazy val combinedCompletions = thisCompletions | q.completions(in)
          this(in) match {
            case Success(_, rest) =>
              // only return optional completions if they start at the last position, otherwise it can behave badly e.g. with fuzzy matching
              if (combinedCompletions.position < rest.pos) Completions.empty
              else combinedCompletions
            case Failure(_, rest) => combinedCompletions
            case Error(_, _) =>
              thisCompletions // avoids backtracking completions in the case of error, e.g. when using the ~! operator
          }
        }
      )
    }

    def completions(in: Input): Completions

    def %>(completions: Elems*): CompletionParser[T] =
      %>(completions.map(el => Completion(el)))

    def %>(completions: Completion): CompletionParser[T] =
      %>(Set(completions))

    def %>(completions: Iterable[Completion]): CompletionParser[T] =
      CompletionParser(in => this(in), in => {
        this(in) match {
          case Failure(_, rest) if rest.atEnd =>
            Completions(rest.pos, CompletionSet(completions))
          case _ => Completions.empty
        }
      })

    def %>(completioner: Input => Completions): CompletionParser[T] =
      CompletionParser(in => this(in), completioner)

    def topCompletions(n: Int): CompletionParser[T] =
      CompletionParser(
        in => this(in),
        in => {
          val completions = this.completions(in)
          Completions(completions.position,
                      completions.sets.mapValues(s =>
                        CompletionSet(s.tag, s.completions.toList.sortBy(_.score).reverse.take(n).toSet)))
        }
      )

    def %(tag: String): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), Some(tag), None, None, None, None))

    def %(score: Int): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), None, Some(score), None, None, None))

    def %(tag: String, score: Int): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), Some(tag), Some(score), None, None, None))

    def %(tag: String, score: Int, description: String): CompletionParser[T] =
      CompletionParser(
        in => this(in),
        in => updateCompletions(this.completions(in), Some(tag), Some(score), Some(description), None, None))

    def %(tag: String, score: Int, description: String, kind: String): CompletionParser[T] =
      CompletionParser(
        in => this(in),
        in => updateCompletions(this.completions(in), Some(tag), Some(score), Some(description), Some(kind), None))

    def %?(description: String): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), None, None, Some(description), None, None))

    def %%(tagKind: String): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), None, None, None, Some(tagKind), None))

    def %-%(kind: String): CompletionParser[T] =
      CompletionParser(in => this(in),
                       in => updateCompletions(this.completions(in), None, None, None, None, Some(kind)))

    def flatMap[U](f: T => CompletionParser[U]): CompletionParser[U] =
      CompletionParser(super.flatMap(f), completions)

    override def map[U](f: T => U): CompletionParser[U] =
      CompletionParser(super.map(f), in => completions(in))

    override def filter(p: T => Boolean): CompletionParser[T] = withFilter(p)

    override def withFilter(p: T => Boolean): CompletionParser[T] =
      CompletionParser(super.withFilter(p), completions)

    private def seqCompletions[U](in: Input, other: => CompletionParser[U]): Completions = {
      lazy val thisCompletions = this.completions(in)
      this(in) match {
        case Success(_, rest) =>
          thisCompletions | other.completions(rest)
        case NoSuccess(_, _) =>
          thisCompletions
      }
    }

    private def updateCompletions(completions: Completions,
                                  newTag: Option[String],
                                  newScore: Option[Int],
                                  newDescription: Option[String],
                                  newTagKind: Option[String],
                                  newKind: Option[String]) = {
      def updateCompletions(completions: Set[Completion], newLinkKind: Option[String]): Set[Completion] = {
        newLinkKind match {
          case Some(x) => completions.map(e => e.copy(kind = Some(x)))
          case None    => completions
        }
      }

      def updateSet(existingSet: CompletionSet) =
        CompletionSet(existingSet.tag.update(newTag, newScore, newDescription, newTagKind),
                      updateCompletions(existingSet.completions, newKind))

      Completions(completions.position,
                  completions.sets.values
                    .map(updateSet)
                    .map(s => s.tag.label -> s)
                    .toMap)
    }

    /** A parser combinator for sequential composition.
      *
      * `p ~ q` succeeds if `p` succeeds and `q` succeeds on the input left over by `p`.
      *
      * @param q a parser that will be executed after `p` (this parser)
      *          succeeds -- evaluated at most once, and only when necessary.
      * @return a `Parser` that -- on success -- returns a `~` (like a `Pair`,
      *         but easier to pattern match on) that contains the result of `p` and
      *         that of `q`. The resulting parser fails if either `p` or `q` fails.
      */
    def ~[U](q: => CompletionParser[U]): CompletionParser[~[T, U]] = {
      lazy val p = q
      CompletionParser(super.~(q), in => seqCompletions(in, p))
    }.named("~")

    /** A parser combinator for sequential composition which keeps only the right result.
      *
      * `p ~> q` succeeds if `p` succeeds and `q` succeeds on the input left over by `p`.
      *
      * @param q a parser that will be executed after `p` (this parser)
      *        succeeds -- evaluated at most once, and only when necessary.
      * @return a `Parser` that -- on success -- returns the result of `q`.
      */
    def ~>[U](q: => CompletionParser[U]): CompletionParser[U] = {
      lazy val p = q
      CompletionParser(super.~>(q), in => seqCompletions(in, p))
    }.named("~>")

    /** A parser combinator for sequential composition which keeps only the left result.
      *
      *  `p <~ q` succeeds if `p` succeeds and `q` succeeds on the input
      *           left over by `p`.
      *
      * @note <~ has lower operator precedence than ~ or ~>.
      *
      * @param q a parser that will be executed after `p` (this parser) succeeds -- evaluated at most once, and only when necessary
      * @return a `Parser` that -- on success -- returns the result of `p`.
      */
    def <~[U](q: => CompletionParser[U]): CompletionParser[T] = {
      lazy val p = q
      CompletionParser(super.<~(q), in => seqCompletions(in, p))
    }.named("<~")

    /** A parser combinator for non-back-tracking sequential composition.
      *
      *  `p ~! q` succeeds if `p` succeeds and `q` succeeds on the input left over by `p`.
      *   In case of failure, no back-tracking is performed (in an earlier parser produced by the `|` combinator).
      *
      * @param p a parser that will be executed after `p` (this parser) succeeds
      * @return a `Parser` that -- on success -- returns a `~` (like a Pair, but easier to pattern match on)
      *         that contains the result of `p` and that of `q`.
      *         The resulting parser fails if either `p` or `q` fails, this failure is fatal.
      */
    def ~![U](q: => CompletionParser[U]): CompletionParser[~[T, U]] = {
      lazy val p = q
      CompletionParser(super.~!(q), in => seqCompletions(in, p))
    }.named("<~")

    /** A parser combinator for non-back-tracking sequential composition which only keeps the right result.
      *
      * `p ~>! q` succeeds if `p` succeds and `q` succeds on the input left over by `p`.
      * In case of failure, no back-tracking is performed (in an earlier parser produced by the `|` combinator).
      *
      * @param q a parser that will be executed after `p` (this parser) succeeds -- evaluated at most once, and only when necessary
      * @return a `Parser` that -- on success -- reutrns the result of `q`.
      *         The resulting parser fails if either `p` or `q` fails, this failure is fatal.
      */
    def ~>![U](q: => CompletionParser[U]): CompletionParser[U] = {
      lazy val p = q
      CompletionParser(super.~>!(q), in => seqCompletions(in, p))
    }.named("~>!")

    /** A parser combinator for non-back-tracking sequential composition which only keeps the left result.
      *
      * `p <~! q` succeeds if `p` succeds and `q` succeds on the input left over by `p`.
      * In case of failure, no back-tracking is performed (in an earlier parser produced by the `|` combinator).
      *
      * @param q a parser that will be executed after `p` (this parser) succeeds -- evaluated at most once, and only when necessary
      * @return a `Parser` that -- on success -- reutrns the result of `p`.
      *         The resulting parser fails if either `p` or `q` fails, this failure is fatal.
      */
    def <~![U](q: => CompletionParser[U]): CompletionParser[T] = {
      lazy val p = q
      CompletionParser(super.<~!(q), in => seqCompletions(in, p))
    }.named("<~!")

    /** A parser combinator for alternative composition.
      *
      *  `p | q` succeeds if `p` succeeds or `q` succeeds.
      *   Note that `q` is only tried if `p`s failure is non-fatal (i.e., back-tracking is allowed).
      *
      * @param q a parser that will be executed if `p` (this parser) fails (and allows back-tracking)
      * @return a `Parser` that returns the result of the first parser to succeed (out of `p` and `q`)
      *         The resulting parser succeeds if (and only if)
      *         - `p` succeeds, ''or''
      *         - if `p` fails allowing back-tracking and `q` succeeds.
      */
    def |[U >: T](q: => CompletionParser[U]): CompletionParser[U] = append(q).named("|")

    /** A parser combinator for alternative with longest match composition.
      *
      *  `p ||| q` succeeds if `p` succeeds or `q` succeeds.
      *  If `p` and `q` both succeed, the parser that consumed the most characters accepts.
      *
      * @param q0 a parser that accepts if p consumes less characters. -- evaluated at most once, and only when necessary
      * @return a `Parser` that returns the result of the parser consuming the most characters (out of `p` and `q`).
      */
    def |||[U >: T](q: => CompletionParser[U]): CompletionParser[U] = {
      lazy val p = q
      CompletionParser(super.|||(q), in => this.completions(in) | p.completions(in))
    }

    /** A parser combinator for function application.
      *
      *  `p ^^ f` succeeds if `p` succeeds; it returns `f` applied to the result of `p`.
      *
      * @param f a function that will be applied to this parser's result (see `map` in `ParseResult`).
      * @return a parser that has the same behaviour as the current parser, but whose result is
      *         transformed by `f`.
      */
    override def ^^[U](f: T => U): CompletionParser[U] =
      CompletionParser(super.^^(f), completions)

    /** A parser combinator that changes a successful result into the specified value.
      *
      *  `p ^^^ v` succeeds if `p` succeeds; discards its result, and returns `v` instead.
      *
      * @param v The new result for the parser, evaluated at most once (if `p` succeeds), not evaluated at all if `p` fails.
      * @return a parser that has the same behaviour as the current parser, but whose successful result is `v`
      */
    override def ^^^[U](v: => U): CompletionParser[U] = {
      CompletionParser(super.^^^(v), completions)
    }.named(toString + "^^^")

    /** A parser combinator for partial function application.
      *
      *  `p ^? (f, error)` succeeds if `p` succeeds AND `f` is defined at the result of `p`;
      *  in that case, it returns `f` applied to the result of `p`. If `f` is not applicable,
      *  error(the result of `p`) should explain why.
      *
      * @param f a partial function that will be applied to this parser's result
      *          (see `mapPartial` in `ParseResult`).
      * @param error a function that takes the same argument as `f` and produces an error message
      *        to explain why `f` wasn't applicable
      * @return a parser that succeeds if the current parser succeeds <i>and</i> `f` is applicable
      *         to the result. If so, the result will be transformed by `f`.
      */
    override def ^?[U](f: PartialFunction[T, U], error: T => String): CompletionParser[U] =
      CompletionParser(super.^?(f, error), completions).named(toString + "^?")

    /** A parser combinator for partial function application.
      *
      *  `p ^? f` succeeds if `p` succeeds AND `f` is defined at the result of `p`;
      *  in that case, it returns `f` applied to the result of `p`.
      *
      * @param f a partial function that will be applied to this parser's result
      *          (see `mapPartial` in `ParseResult`).
      * @return a parser that succeeds if the current parser succeeds <i>and</i> `f` is applicable
      *         to the result. If so, the result will be transformed by `f`.
      */
    override def ^?[U](f: PartialFunction[T, U]): CompletionParser[U] =
      CompletionParser(super.^?(f), completions)

    /** A parser combinator that parameterizes a subsequent parser with the
      *  result of this one.
      *
      *  Use this combinator when a parser depends on the result of a previous
      *  parser. `p` should be a function that takes the result from the first
      *  parser and returns the second parser.
      *
      *  `p into fq` (with `fq` typically `{x => q}`) first applies `p`, and
      *  then, if `p` successfully returned result `r`, applies `fq(r)` to the
      *  rest of the input.
      *
      *  ''From: G. Hutton. Higher-order functions for parsing. J. Funct. Program., 2(3):323--343, 1992.''
      *
      *  @example {{{
      *  def perlRE = "m" ~> (".".r into (separator => """[^%s]*""".format(separator).r <~ separator))
      *  }}}
      *
      *  @param fq a function that, given the result from this parser, returns
      *         the second parser to be applied
      *  @return a parser that succeeds if this parser succeeds (with result `x`)
      *          and if then `fq(x)` succeeds
      */
    override def into[U](fq: T => Parser[U]): CompletionParser[U] =
      CompletionParser(super.into(fq), completions)

    /** Changes the failure message produced by a parser.
      *
      *  This doesn't change the behavior of a parser on neither
      *  success nor error, just on failure. The semantics are
      *  slightly different than those obtained by doing `| failure(msg)`,
      *  in that the message produced by this method will always
      *  replace the message produced, which is not guaranteed
      *  by that idiom.
      *
      *  For example, parser `p` below will always produce the
      *  designated failure message, while `q` will not produce
      *  it if `sign` is parsed but `number` is not.
      *
      *  {{{
      *  def p = sign.? ~ number withFailureMessage  "Number expected!"
      *  def q = sign.? ~ number | failure("Number expected!")
      *  }}}
      *
      *  @param msg The message that will replace the default failure message.
      *  @return    A parser with the same properties and different failure message.
      */
    override def withErrorMessage(msg: String) =
      CompletionParser(super.withErrorMessage(msg), completions)

  }

  /** Wrap a parser so that its failures become errors (the `|` combinator
    *  will give up as soon as it encounters an error, on failure it simply
    *  tries the next alternative).
    */
  def commit[T](p: => CompletionParser[T]): CompletionParser[T] =
    CompletionParser(super.commit(p), p.completions)

  /** A parser matching input elements that satisfy a given predicate.
    *
    *  `elem(kind, p)` succeeds if the input starts with an element `e` for which `p(e)` is true.
    *
    *  @param  kind   The element kind, used for error messages
    *  @param  p      A predicate that determines which elements match.
    *  @param  completions Possible alternatives (for completion)
    *  @return
    */
  def elem(kind: String, p: Elem => Boolean, completions: Seq[Elem] = Nil): CompletionParser[Elem] =
    acceptIf(p,
             if (completions.isEmpty)
               None
             else
               Some(CompletionSet(CompletionTag(kind), completions.map(c => Completion(c)).toSet)))(inEl =>
      kind + " expected")

  /** A parser that matches only the given element `e`.
    *
    *  The method is implicit so that elements can automatically be lifted to their parsers.
    *  For example, when parsing `Token`s, `Identifier("new")` (which is a `Token`) can be used directly,
    *  instead of first creating a `Parser` using `accept(Identifier("new"))`.
    *
    *  @param e the `Elem` that must be the next piece of input for the returned parser to succeed
    *  @return a `tParser` that succeeds if `e` is the next available input.
    */
  override implicit def accept(e: Elem): CompletionParser[Elem] =
    acceptIf(_ == e, Some(CompletionSet(e)))("'" + e + "' expected but " + _ + " found")

  /** A parser that matches only the given list of element `es`.
    *
    * `accept(es)` succeeds if the input subsequently provides the elements in the list `es`.
    *
    * @param  es the list of expected elements
    * @return a Parser that recognizes a specified list of elements
    */
  override def accept[ES <% List[Elem]](es: ES): CompletionParser[List[Elem]] = acceptSeq(es)

  /** The parser that matches an element in the domain of the partial function `f`.
    *
    *  If `f` is defined on the first element in the input, `f` is applied
    *  to it to produce this parser's result.
    *
    *  Example: The parser `accept("name", {case Identifier(n) => Name(n)})`
    *          accepts an `Identifier(n)` and returns a `Name(n)`
    *
    *  @param expected a description of the kind of element this parser expects (for error messages)
    *  @param f a partial function that determines when this parser is successful and what its output is
    *  @param completions Possible alternatives (for completion)
    *  @return A parser that succeeds if `f` is applicable to the first element of the input,
    *          applying `f` to it to produce the result.
    */
  def accept[U](expected: String, f: PartialFunction[Elem, U], completions: Set[Elem] = Set()): CompletionParser[U] =
    acceptMatch(expected, f, completions.map(Completion(_)))

  /** A parser matching input elements that satisfy a given predicate.
    *
    *  `acceptIf(p)(el => "Unexpected "+el)` succeeds if the input starts with an element `e` for which `p(e)` is true.
    *
    *  @param  err    A function from the received element into an error message.
    *  @param  p      A predicate that determines which elements match.
    *  @param completions Possible completions
    *  @return        A parser for elements satisfying p(e).
    */
  def acceptIf(p: Elem => Boolean, completions: Option[CompletionSet])(err: Elem => String): CompletionParser[Elem] = {
    CompletionParser(super.acceptIf(p)(err),
                     in => completions.map(Completions(in.pos, _)).getOrElse(Completions.empty))
  }

  def acceptMatch[U](expected: String,
                     f: PartialFunction[Elem, U],
                     completions: Set[Completion]): CompletionParser[U] = {
    lazy val completionSet =
      if (completions.nonEmpty)
        Some(CompletionSet(CompletionTag(expected), completions))
      else None
    CompletionParser(super.acceptMatch(expected, f),
                     in => completionSet.map(Completions(in.pos, _)).getOrElse(Completions.empty))
      .named(expected)
  }

  /** A parser that matches only the given [[scala.collection.Iterable]] collection of elements `es`.
    *
    * `acceptSeq(es)` succeeds if the input subsequently provides the elements in the iterable `es`.
    *
    * @param  es the list of expected elements
    * @return a Parser that recognizes a specified list of elements
    */
  override def acceptSeq[ES <% Iterable[Elem]](es: ES): CompletionParser[List[Elem]] =
    CompletionParser(super.acceptSeq(es),
                     in =>
                       es.tail
                         .foldLeft(accept(es.head))((a, b) => a ~> accept(b))
                         .completions(in))

  /** A parser that always fails.
    *
    * @param msg The error message describing the failure.
    * @return A parser that always fails with the specified error message.
    */
  override def failure(msg: String): CompletionParser[Nothing] =
    CompletionParser(super.failure(msg), _ => Completions.empty)

  /** A parser that always succeeds.
    *
    * @param v The result for the parser
    * @return A parser that always succeeds, with the given result `v`
    */
  override def success[T](v: T): CompletionParser[T] = CompletionParser(super.success(v), _ => Completions.empty)

  /** A parser that results in an error.
    *
    * @param msg The error message describing the failure.
    * @return A parser that always fails with the specified error message.
    */
  override def err(msg: String): CompletionParser[Nothing] = CompletionParser(super.err(msg), _ => Completions.empty)

  /** A helper method that turns a `Parser` into one that will
    * print debugging information to stdout before and after
    * being applied.
    */
  def log[T](p: => CompletionParser[T])(name: String): CompletionParser[T] =
    CompletionParser(super.log(p)(name), p.completions)

  /** A parser generator for repetitions.
    *
    * `rep(p)` repeatedly uses `p` to parse the input until `p` fails
    * (the result is a List of the consecutive results of `p`).
    *
    * @param p a `Parser` that is to be applied successively to the input
    * @return A parser that returns a list of results produced by repeatedly applying `p` to the input.
    */
  def rep[T](p: => CompletionParser[T]): CompletionParser[List[T]] = rep1(p) | success(List())

  /** A parser generator for interleaved repetitions.
    *
    *  `repsep(p, q)` repeatedly uses `p` interleaved with `q` to parse the input, until `p` fails.
    *  (The result is a `List` of the results of `p`.)
    *
    *  Example: `repsep(term, ",")` parses a comma-separated list of term's, yielding a list of these terms.
    *
    * @param p a `Parser` that is to be applied successively to the input
    * @param q a `Parser` that parses the elements that separate the elements parsed by `p`
    * @return A parser that returns a list of results produced by repeatedly applying `p` (interleaved with `q`) to the input.
    *         The results of `p` are collected in a list. The results of `q` are discarded.
    */
  def repsep[T](p: => CompletionParser[T], q: => CompletionParser[Any]): CompletionParser[List[T]] =
    rep1sep(p, q) | success(List())

  /** A parser generator for non-empty repetitions.
    *
    *  `rep1(p)` repeatedly uses `p` to parse the input until `p` fails -- `p` must succeed at least
    *             once (the result is a `List` of the consecutive results of `p`)
    *
    * @param p a `Parser` that is to be applied successively to the input
    * @return A parser that returns a list of results produced by repeatedly applying `p` to the input
    *        (and that only succeeds if `p` matches at least once).
    */
  def rep1[T](p: => CompletionParser[T]): CompletionParser[List[T]] = rep1(p, p)

  /** A parser generator for non-empty repetitions.
    *
    *  `rep1(f, p)` first uses `f` (which must succeed) and then repeatedly
    *     uses `p` to parse the input until `p` fails
    *     (the result is a `List` of the consecutive results of `f` and `p`)
    *
    * @param first a `Parser` that parses the first piece of input
    * @param p0 a `Parser` that is to be applied successively to the rest of the input (if any) -- evaluated at most once, and only when necessary
    * @return A parser that returns a list of results produced by first applying `f` and then
    *         repeatedly `p` to the input (it only succeeds if `f` matches).
    */
  def rep1[T](first: => CompletionParser[T], p0: => CompletionParser[T]): CompletionParser[List[T]] = {
    lazy val p = p0 // lazy argument
    CompletionParser(
      super.rep1(first, p0),
      in => {
        @tailrec def continue(in: Input): Completions = {
          p(in) match {
            case Success(_, rest) => continue(rest)
            case NoSuccess(_, _)  => p.completions(in)
          }
        }
        first(in) match {
          case Success(_, rest) => continue(rest)
          case NoSuccess(_, _)  => first.completions(in)
        }
      }
    )
  }

  /** A parser generator for a specified number of repetitions.
    *
    *  `repN(n, p)` uses `p` exactly `n` time to parse the input
    *  (the result is a `List` of the `n` consecutive results of `p`).
    *
    * @param p0   a `Parser` that is to be applied successively to the input
    * @param num the exact number of times `p` must succeed
    * @return    A parser that returns a list of results produced by repeatedly applying `p` to the input
    *        (and that only succeeds if `p` matches exactly `n` times).
    */
  def repN[T](num: Int, p0: => CompletionParser[T]): CompletionParser[List[T]] = {
    lazy val p = p0 // lazy argument
    if (num == 0) success(Nil)
    else
      CompletionParser(
        super.repN(num, p0),
        in => {
          var parsedCount = 0
          @tailrec def completions(in0: Input): Completions =
            if (parsedCount == num) Completions.empty
            else
              p(in0) match {
                case Success(_, rest) => parsedCount += 1; completions(rest)
                case ns: NoSuccess    => p.completions(in0)
              }

          val result = completions(in)
          if (parsedCount < num) result else Completions.empty
        }
      )
  }

  /** A parser generator for non-empty repetitions.
    *
    *  `rep1sep(p, q)` repeatedly applies `p` interleaved with `q` to parse the
    *  input, until `p` fails. The parser `p` must succeed at least once.
    *
    * @param p a `Parser` that is to be applied successively to the input
    * @param q a `Parser` that parses the elements that separate the elements parsed by `p`
    *          (interleaved with `q`)
    * @return A parser that returns a list of results produced by repeatedly applying `p` to the input
    *         (and that only succeeds if `p` matches at least once).
    *         The results of `p` are collected in a list. The results of `q` are discarded.
    */
  def rep1sep[T](p: => CompletionParser[T], q: => CompletionParser[Any]): CompletionParser[List[T]] =
    p ~ rep(q ~> p) ^^ { case x ~ y => x :: y }

  /** A parser generator that, roughly, generalises the rep1sep generator so
    *  that `q`, which parses the separator, produces a left-associative
    *  function that combines the elements it separates.
    *
    *  ''From: J. Fokker. Functional parsers. In J. Jeuring and E. Meijer, editors, Advanced Functional Programming,
    *  volume 925 of Lecture Notes in Computer Science, pages 1--23. Springer, 1995.''
    *
    * @param p a parser that parses the elements
    * @param q a parser that parses the token(s) separating the elements, yielding a left-associative function that
    *          combines two elements into one
    */
  def chainl1[T](p: => CompletionParser[T], q: => CompletionParser[(T, T) => T]): CompletionParser[T] =
    chainl1(p, p, q)

  /** A parser generator that, roughly, generalises the `rep1sep` generator
    *  so that `q`, which parses the separator, produces a left-associative
    *  function that combines the elements it separates.
    *
    * @param first a parser that parses the first element
    * @param p a parser that parses the subsequent elements
    * @param q a parser that parses the token(s) separating the elements,
    *          yielding a left-associative function that combines two elements
    *          into one
    */
  def chainl1[T, U](first: => CompletionParser[T],
                    p: => CompletionParser[U],
                    q: => CompletionParser[(T, U) => T]): CompletionParser[T] = first ~ rep(q ~ p) ^^ {
    case x ~ xs =>
      xs.foldLeft(x: T) { case (a, f ~ b) => f(a, b) } // x's type annotation is needed to deal with changed type inference due to SI-5189
  }

  /** A parser generator that generalises the `rep1sep` generator so that `q`,
    *  which parses the separator, produces a right-associative function that
    *  combines the elements it separates. Additionally, the right-most (last)
    *  element and the left-most combining function have to be supplied.
    *
    * rep1sep(p: Parser[T], q) corresponds to chainr1(p, q ^^ cons, cons, Nil) (where val cons = (x: T, y: List[T]) => x :: y)
    *
    * @param p a parser that parses the elements
    * @param q a parser that parses the token(s) separating the elements, yielding a right-associative function that
    *          combines two elements into one
    * @param combine the "last" (left-most) combination function to be applied
    * @param first   the "first" (right-most) element to be combined
    */
  def chainr1[T, U](p: => CompletionParser[T],
                    q: => CompletionParser[(T, U) => U],
                    combine: (T, U) => U,
                    first: U): CompletionParser[U] = p ~ rep(q ~ p) ^^ {
    case x ~ xs => (new ~(combine, x) :: xs).foldRight(first) { case (f ~ a, b) => f(a, b) }
  }

  /** A parser generator for optional sub-phrases.
    *
    *  `opt(p)` is a parser that returns `Some(x)` if `p` returns `x` and `None` if `p` fails.
    *
    * @param p A `Parser` that is tried on the input
    * @return a `Parser` that always succeeds: either with the result provided by `p` or
    *         with the empty result
    */
  def opt[T](p: => CompletionParser[T]): CompletionParser[Option[T]] =
    p ^^ (x => Some(x)) | success(None)

  /** Wrap a parser so that its failures and errors become success and
    *  vice versa -- it never consumes any input.
    */
  def not[T](p: => CompletionParser[T]): CompletionParser[Unit] =
    CompletionParser(super.not(p), _ => Completions.empty)

  /** A parser generator for guard expressions. The resulting parser will
    *  fail or succeed just like the one given as parameter but it will not
    *  consume any input.
    *
    * @param p a `Parser` that is to be applied to the input
    * @return A parser that returns success if and only if `p` succeeds but
    *         never consumes any input
    */
  def guard[T](p: => CompletionParser[T]): CompletionParser[T] =
    CompletionParser(super.guard(p), p.completions)

  /** `positioned` decorates a parser's result with the start position of the
    *  input it consumed.
    *
    * @param p a `Parser` whose result conforms to `Positional`.
    * @return A parser that has the same behaviour as `p`, but which marks its
    *         result with the start position of the input it consumed,
    *         if it didn't already have a position.
    */
  def positioned[T <: Positional](p: => CompletionParser[T]): CompletionParser[T] =
    CompletionParser(super.positioned(p), p.completions)

  /** A parser generator delimiting whole phrases (i.e. programs).
    *
    *  `phrase(p)` succeeds if `p` succeeds and no input is left over after `p`.
    *
    *  @param p the parser that must consume all input for the resulting parser
    *           to succeed.
    *  @return  a parser that has the same result as `p`, but that only succeeds
    *           if `p` consumed all the input.
    */
  def phrase[T](p: CompletionParser[T]) =
    CompletionParser(super.phrase(p), p.completions)
}
