/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util.parsing.input

private[input] trait PositionCache {
  private lazy val indexCacheTL =
    // not DynamicVariable as that would share the map from parent to child :-(
    new ThreadLocal[java.util.Map[CharSequence, Array[Int]]] {
      override def initialValue = new java.util.WeakHashMap[CharSequence, Array[Int]]
    }

  private[input] def indexCache = indexCacheTL.get
}
