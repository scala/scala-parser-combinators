import scala.util.parsing.combinator._
import scala.util.DynamicVariable

import org.junit.Test

class t9010 {
  @Test
  def test: Unit = {
    val p = new grammar
    val lastNoSuccessVar = getLastNoSuccessVar(p)
    import p._

    val res1 = parse(x, "x")
    assert(res1.successful)
    assert(lastNoSuccessVar.value == None)

    val res2 = parse(x, "y")
    assert(!res2.successful)
    assert(lastNoSuccessVar.value == None)

    val res3 = parseAll(x, "x")
    assert(res3.successful)
    assert(lastNoSuccessVar.value == None)

    val res4 = parseAll(x, "y")
    assert(!res4.successful)
    assert(lastNoSuccessVar.value == None)
  }

  private def getLastNoSuccessVar(p: Parsers): DynamicVariable[Option[_]] = {
    // use java reflection instead of scala (see below) because of
    // https://issues.scala-lang.org/browse/SI-9306
    val fn = "scala$util$parsing$combinator$Parsers$$lastNoSuccessVar"
    val f = p.getClass.getDeclaredMethod(fn)
    f.setAccessible(true)
    f.invoke(p).asInstanceOf[DynamicVariable[Option[_]]]

    /*
    val ru = scala.reflect.runtime.universe
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val lastNoSuccessVarField =
      ru.typeOf[Parsers].decl(ru.TermName("lastNoSuccessVar")).asTerm.accessed.asTerm
    mirror.reflect(p).reflectField(lastNoSuccessVarField).get.
      asInstanceOf[DynamicVariable[Option[_]]]
    */
  }

  private final class grammar extends RegexParsers {
    val x: Parser[String] = "x"
  }
}
