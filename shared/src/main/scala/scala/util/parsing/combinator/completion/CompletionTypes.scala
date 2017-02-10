/*                                                      *\
**  scala-parser-combinators completion extensions      **
**  Copyright (c) by Nexthink S.A.                      **
**  Lausanne, Switzerland (http://www.nexthink.com)     **
\*                                                      */

package scala.util.parsing.combinator.completion

import scala.util.parsing.input.{NoPosition, Position}

/**  Collection of data types allowing definition of structured parser completions.
  *  A `Completions` instance can contain multiple `CompletionSet`s instances. A `CompletionSet` provides a set of
  *  `Completion` entries and is tagged with a `CompletionTag`.
  *
  *  Sets allow structuring the completion entries into groups, each group tagged with a `label` (plus optional
  *  `description` and `kind`, the latter allowing e.g. encoding visual attributes for the set).
  *  Sets also feature a score, which defines the order between sets within the `Completions` instance.
  *
  *  Each `Completion` entry within a set has a `value`, a `score` and a `kind`:
  *  the score allows ordering the entries within a set, and the kind can e.g. be used to assign a representation style
  *  for a particular completion entry.
  *
  *  Note that specifying tags and sets is optional: if no tag is specified upon creation,
  *  `Completions` instances create a unique default set with an empty tag.
  *
  *  @author Jonas Chapuis
  */
trait CompletionTypes {
  type Elem

  val DefaultCompletionTag   = ""
  val DefaultCompletionScore = 0

  /** Tag defining identification and attributes of a set of completion entries
    * @param label tag label
    * @param score tag score (the higher the better, 0 by default)
    * @param description tag description (optional) - can be used for additional information e.g. for a tooltip
    * @param kind tag kind (optional) - can be used e.g. to define visual style
    */
  case class CompletionTag(label: String, score: Int, description: Option[String], kind: Option[String]) {
    def update(newTag: Option[String],
               newScore: Option[Int],
               newDescription: Option[String],
               newKind: Option[String]) =
      copy(
        label = newTag.getOrElse(label),
        score = newScore.getOrElse(score),
        description = newDescription.map(Some(_)).getOrElse(description),
        kind = newKind.map(Some(_)).getOrElse(kind)
      )

    override def toString: String = label
  }

  case object CompletionTag {
    val Default =
      CompletionTag(DefaultCompletionTag, DefaultCompletionScore, None, None)
    def apply(label: String): CompletionTag =
      CompletionTag(label, DefaultCompletionScore, None, None)
    def apply(label: String, score: Int): CompletionTag =
      CompletionTag(label, score, None, None)
  }

  /** Set of related completion entries
    * @param tag set tag
    * @param completions set of unique completion entries
    */
  case class CompletionSet(tag: CompletionTag, completions: Set[Completion]) {
    require(completions.nonEmpty, "empty completions set")
    def label: String               = tag.label
    def score: Int                  = tag.score
    def description: Option[String] = tag.description
    def kind: Option[String]        = tag.kind
    def completionStrings: Seq[String] =
      completions.toSeq.sorted.map(_.value.toString)
  }

  case object CompletionSet {
    def apply(tag: String, el: Elem): CompletionSet =
      CompletionSet(CompletionTag(tag), Set(Completion(el)))

    def apply(tag: String, elems: Elems): CompletionSet =
      CompletionSet(CompletionTag(tag), Set(Completion(elems)))

    def apply(tag: String, completion: Completion): CompletionSet =
      CompletionSet(CompletionTag(tag), Set(completion))

    def apply(tag: String, completions: Iterable[Completion]): CompletionSet =
      CompletionSet(CompletionTag(tag), completions.toSet)

    def apply(completions: Iterable[Completion]): CompletionSet =
      CompletionSet(CompletionTag.Default, completions.toSet)

    def apply(completions: Completion*): CompletionSet =
      CompletionSet(CompletionTag.Default, completions.toSet)

    def apply(el: Elem): CompletionSet =
      CompletionSet(CompletionTag.Default, Set(Completion(el)))

    def apply(completions: Traversable[Elems]): CompletionSet =
      CompletionSet(CompletionTag.Default, completions.map(Completion(_)).toSet)
  }

  type Elems = Seq[Elem]

  /** Completion entry
    * @param value entry value (e.g. string literal)
    * @param score entry score (defines the order of entries within a set, the higher the better)
    * @param kind entry kind (e.g. visual style)
    */
  case class Completion(value: Elems, score: Int = DefaultCompletionScore, kind: Option[String] = None) {
    require(value.nonEmpty, "empty completion")
    def updateKind(newKind: Option[String]) =
      copy(kind = newKind.map(Some(_)).getOrElse(kind))
  }
  case object Completion {
    def apply(el: Elem): Completion = Completion(Seq(el))
    implicit def orderingByScoreAndThenAlphabetical: Ordering[Completion] =
      Ordering.by(c => (-c.score, c.value.toString))
  }

  /** Result of parser completion, listing the possible entry alternatives at a certain input position
    * @param position position in the input where completion entries apply
    * @param sets completion entries, grouped per tag
    */
  case class Completions(position: Position, sets: Map[String, CompletionSet]) {
    def isEmpty: Boolean                               = sets.isEmpty
    def nonEmpty: Boolean                              = !isEmpty
    def setWithTag(tag: String): Option[CompletionSet] = sets.get(tag)
    def allSets: Iterable[CompletionSet]               = sets.values
    def defaultSet: Option[CompletionSet]              = sets.get("")

    private def unionSets(left: CompletionSet, right: CompletionSet): CompletionSet = {
      def offsetCompletions(set: CompletionSet) = {
        val isOffsetRequired =
          set.completions.map(_.score).exists(_ < set.score)
        if (isOffsetRequired)
          set.completions.map(c => Completion(c.value, set.score + c.score, c.kind))
        else set.completions
      }
      CompletionSet(
        CompletionTag(left.tag.label, left.score.min(right.score), left.description, left.kind.orElse(right.kind)),
        offsetCompletions(left) ++ offsetCompletions(right)
      )
    }

    private def mergeCompletions(other: Completions) = {
      val overlappingSetTags = sets.keySet.intersect(other.sets.keySet)
      val unions =
        overlappingSetTags.map(name => (sets(name), other.sets(name))).map {
          case (left, right) => unionSets(left, right)
        }
      val leftExclusive = sets.keySet.diff(overlappingSetTags).map(sets(_))
      val rightExclusive =
        other.sets.keySet.diff(overlappingSetTags).map(other.sets(_))
      Completions(position,
                  (unions ++ leftExclusive ++ rightExclusive)
                    .map(s => s.tag.label -> s)
                    .toMap)
    }

    def |(other: Completions): Completions = {
      other match {
        case Completions.empty => this
        case _ =>
          other.position match {
            case otherPos if otherPos < position  => this
            case otherPos if otherPos == position => mergeCompletions(other)
            case _                                => other
          }
      }
    }

    def completionStrings: Seq[String] =
      sets.values.toSeq
        .sortBy(_.score)
        .reverse
        .flatMap(_.completionStrings)
        .toList

    def takeTop(count: Int): Completions = {
      val allEntries = allSets
        .flatMap(s => s.completions.map((_, s.tag)))
        .toList
      val sortedEntries =
        allEntries
          .sortBy {
            case (Completion(_, score, kind), CompletionTag(_, tagScore, _, _)) =>
              (tagScore, score)
          }
          .reverse
          .take(count)
      val regroupedSets = sortedEntries
        .groupBy { case (_, tag) => tag }
        .map {
          case (groupTag, completions) =>
            CompletionSet(groupTag, completions.map(_._1).toSet)
        }
      copy(sets = regroupedSets.map(s => (s.tag.label, s)).toMap)
    }

    def setsScoredWithMaxCompletion(): Completions = {
      Completions(
        position,
        sets.mapValues(s => CompletionSet(s.tag.copy(score = s.completions.map(_.score).max), s.completions)))
    }
  }

  case object Completions {
    def apply(position: Position, completionSet: CompletionSet): Completions =
      Completions(position, Map(completionSet.tag.label -> completionSet))
    def apply(position: Position, completions: Traversable[Elems]): Completions =
      Completions(position, CompletionSet(completions))
    def apply(completionSet: CompletionSet): Completions =
      Completions(NoPosition, completionSet)
    def apply(completionSets: Iterable[CompletionSet]): Completions =
      Completions(NoPosition, completionSets.map(s => s.tag.label -> s).toMap)

    val empty = Completions(NoPosition, Map[String, CompletionSet]())
  }

}
