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
      JObject(res) <- arr
      JField("resultType", JString(rtype)) <- res

      JField("parent", JObject(parent)) <- res
      JField("name", JString(pname)) <- parent
      JField("typeParams", JString(ptparams)) <- parent

      JField("name", JString(name)) <- res
      JField("typeParams", JString(tparams)) <- res

      JField("comment", JObject(comment)) <- res
      JField("short", JObject(short)) <- comment
      JField("txt", JString(txt)) <- short

      JField("valueParams", JString(vparams)) <- res
    } yield Result(pname, ptparams, name, tparams, vparams, rtype, txt)

  }

  case class Result(pname: String, ptparams: String, name: String, tparams: String, vparams: String, rtype: String, txt: String) {
    def toStringShort =  pname + ptparams + " " + name + tparams + ": " + vparams + ": " + rtype

    def toStringFull = toStringShort + "\n" + txt
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
