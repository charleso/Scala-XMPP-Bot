package drashid.agent

import net.liftweb.json.JsonAST.{JString, JArray, JField, JObject}
import net.liftweb.json.JsonParser
import dispatch.{Http, url}

object Ls {
  def query(query: String) = {
    val u = url("http://ls.implicit.ly/api/1/search") <<? Map("q" -> query, "limit" -> "10")
    val source = Http(u as_str)
    val results = for {
      JObject(obj) <- JsonParser.parse(source)
      JField("organization", JString(org)) <- obj
      JField("name", JString(name)) <- obj
      JField("description", JString(desc)) <- obj
      JField("versions", JArray(versions)) <- obj
      JObject(vobj) <- versions
      JField("version", JString(version)) <- vobj
    } yield Result(org, name, desc, version)
    results.groupBy(r => r.org + r.name).mapValues(_.head).values
  }

  case class Result(val org: String, val name: String, val desc: String, val version: String) {
    def toStringShort = "%s %s (%s)" format(org, name, version)

    def toStringLong = "%s '%s'" format(toStringShort, desc)
  }

}

case class Ls() extends CommandAgent {

  import Ls.Result

  def handle = {
    case CommandData("ls", data) => query(data)(_.toStringShort)
    case CommandData("ls-long", data) => query(data)(_.toStringLong)
    case _ => None
  }

  def query(data: String)(f: Result => String) = try {
    Some(Ls.query(data).map(f).mkString("\n"))
  } catch {
    case e: Exception => Some("'%s' not found" format data)
  }
}
