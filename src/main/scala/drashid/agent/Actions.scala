package drashid.agent

import dispatch.{url, Http}
import util.matching.Regex.{Match}
import akka.event.EventHandler

/*
 Let Me Google That For You
*/
case class Google() extends Agent{
  def handle = {
    case Command("google", data) => Some("http://lmgtfy.com/?q=" + normalize(data))
    case _ => None
  }
  def normalize(data: String) = data.replaceAll(" ", "+")
}

/*
   UmbrellaToday.com
 */
case class Umbrella() extends Agent{
  def handle = {
    case Command("umbrella", data) => find()
    case _ => None
  }

  private def find() = {
    val AnswerPattern = """<span>(YES|NO)</span>""".r
    val u = url("http://umbrellatoday.com/locations/596360971/forecast")
    val source = Http(u as_str)
    AnswerPattern.findFirstMatchIn(source) match {
      case Some(found: Match) => Some(found.group(1))
      case _ => None
    }
  }
}