scala-parser-combinators [<img src="https://img.shields.io/travis/scala/scala-parser-combinators.svg"/>](https://travis-ci.org/scala/scala-parser-combinators) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.11.svg?label=latest%20release%20for%202.11"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.11) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.12*.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.12*) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scala/scala-parser-combinators)
========================

### Scala Standard Parser Combinator Library

This library is now community-maintained. If you are interested in helping please contact @gourlaysama or mention it [on Gitter](https://gitter.im/scala/scala-parser-combinators).

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use Parser Combinators.

## Documentation

 * A (perhaps somewhat outdated) [Getting Started](https://wiki.scala-lang.org/display/SW/Parser+Combinators--Getting+Started)
 * [Current API](http://www.scala-lang.org/files/archive/api/current/scala-parser-combinators/scala/util/parsing/combinator)

## Adding an SBT dependency
To depend on scala-parser-combinators in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
```

(Assuming you're using a `scalaVersion` for which a scala-parser-combinators is published. The first 2.11 milestone for which this is true is 2.11.0-M4.)

To support multiple Scala versions, see the example in https://github.com/scala/scala-module-dependency-sample.

## Contributing

 * See the [Scala Developer Guidelines](https://github.com/scala/scala/blob/2.12.x/CONTRIBUTING.md) for general contributing guidelines
 * Have a look at [existing issues](https://github.com/scala/scala-parser-combinators/issues)
 * Ask questions and discuss [on Gitter](https://gitter.im/scala/scala-parser-combinators)
