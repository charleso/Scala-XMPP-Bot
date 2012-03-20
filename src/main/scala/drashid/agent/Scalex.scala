package drashid.agent

import net.liftweb.json.JsonAST.{JArray, JField, JObject, JString}
import net.liftweb.json.JsonParser
import dispatch.{Http, url}

// https://github.com/ornicar/scalex/blob/master/http-api-documentation.md
object Scalex {
  def query(query: String) = {
    val u = url("http://api.scalex.org/") <<? Map("q" -> query)
    val source = Http(u as_str)
    for {
      JObject(obj) <- JsonParser.parse(source)
      JField("results", JArray(arr)) <- obj
      r@JObject(res) <- arr
      JField("resultType", JString(rtype)) <- res

      JField("parent", JObject(parent)) <- res
      JField("qualifiedName", JString(pname)) <- parent
      JField("typeParams", JString(ptparams)) <- parent

      JField("name", JString(name)) <- res
      JField("typeParams", JString(tparams)) <- res

      JField("valueParams", JString(vparams)) <- res
    } yield {
      val txt = (r \ "comment" \ "short" \ "txt") match {
        case JString(txt) => Some(txt)
        case _ => None
      }
      Result(pname, ptparams, name, tparams, vparams, rtype, txt)
    }

  }

  case class Result(pname: String, ptparams: String, name: String, tparams: String, vparams: String, rtype: String, txt: Option[String]) {
    def toStringShort =  pname + ptparams + " " + name + tparams + ": " + vparams + ": " + rtype

    def toStringFull = toStringShort + txt.map("\n" + _).getOrElse("")
  }

}

case class Scalex() extends CommandAgent {

  import Scalex.Result

  def handle = {
    case CommandData("scalex", data) => query(data)(_.toStringShort)
    case CommandData("scalex-full", data) => query(data)(_.toStringFull)
    case _ => None
  }

  def query(data: String)(f: Result => String) = Some(Scalex.query(data).map(f).mkString("\n"))
}
