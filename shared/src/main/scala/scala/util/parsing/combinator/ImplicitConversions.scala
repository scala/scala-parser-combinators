/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala
package util.parsing.combinator

import scala.language.implicitConversions

/** This object contains implicit conversions that come in handy when using the `^^` combinator.
 *
 *  Refer to [[scala.util.parsing.combinator.Parsers]] to construct an AST from the concrete syntax.
 *
 * The reason for this is that the sequential composition combinator (`~`) combines its constituents
 * into a ~. When several `~`s are combined, this results in nested `~`s (to the left).
 * The `flatten*` coercions makes it easy to apply an `n`-argument function to a nested `~` of
 * depth `n-1`
 *
 * The `headOptionTailToFunList` converts a function that takes a `List[A]` to a function that
 * accepts a `~[A, Option[List[A]]]` (this happens when parsing something of the following
 * shape: `p ~ opt("." ~ repsep(p, "."))` -- where `p` is a parser that yields an `A`).
 */
trait ImplicitConversions { self: Parsers =>
  implicit def flatten2[A, B, C]         (f: (A, B) => C): A ~ B => C =
    (p: ~[A, B]) => p match {case a ~ b => f(a, b)}
  implicit def flatten3[A, B, C, D]      (f: (A, B, C) => D): A ~ B ~ C => D =
    (p: ~[~[A, B], C]) => p match {case a ~ b ~ c => f(a, b, c)}
  implicit def flatten4[A, B, C, D, E]   (f: (A, B, C, D) => E): A ~ B ~ C ~ D => E =
    (p: ~[~[~[A, B], C], D]) => p match {case a ~ b ~ c ~ d => f(a, b, c, d)}
  implicit def flatten5[A, B, C, D, E, F](f: (A, B, C, D, E) => F): A ~ B ~ C ~ D ~ E => F =
    (p: ~[~[~[~[A, B], C], D], E]) => p match {case a ~ b ~ c ~ d ~ e=> f(a, b, c, d, e)}
  implicit def headOptionTailToFunList[A, T] (f: List[A] => T): A ~ Option[List[A]] => T =
    (p: ~[A, Option[List[A]]]) => f(p._1 :: (p._2 match { case Some(xs) => xs case None => Nil}))
}
