package drashid.agent

import dispatch.{url, Http}
import util.matching.Regex.{Match}

/**
 * Simple Greeting
 */
case class Greet() extends CommandAgent {
  def handle = {
    case CommandData("greet", data) => Some("Hi.")
    case _ => None
  }
}

/**
 * Let Me Google That For You
 */
case class Google() extends CommandAgent {
  def handle = {
    case CommandData("google", data) => Some("http://lmgtfy.com/?q=" + normalize(data))
    case _ => None
  }
  def normalize(data: String) = data.replaceAll(" ", "+")
}

/**
 * UmbrellaToday.com (NYC only)
 */
case class Umbrella() extends CommandAgent {
  def handle = {
    case CommandData("umbrella", data) => find()
    case _ => None
  }

  private def find() = {
    val AnswerPattern = """<h3>\s*<span>(YES|NO)</span>\s*</h3>""".r
    val u = url("http://umbrellatoday.com/locations/596360971/forecast")
    val source = Http(u as_str)
    AnswerPattern.findFirstMatchIn(source) match {
      case Some(found: Match) => Some(found.group(1))
      case _ => None
    }
  }
}