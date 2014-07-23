scala-parser-combinators [<img src="https://api.travis-ci.org/scala/scala-parser-combinators.png"/>](https://travis-ci.org/scala/scala-parser-combinators)
========================

Scala Standard Parser Combinator Library

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use Parser Combinators.

## Adding an SBT dependency
To depend on scala-parser-combinators in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
```

(Assuming you're using a `scalaVersion` for which a scala-parser-combinators is published. The first 2.11 milestone for which this is true is 2.11.0-M4.)

To support multiple Scala versions, see the example in https://github.com/scala/scala-module-dependency-sample.
