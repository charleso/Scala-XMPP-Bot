package drashid.agent

import tools.nsc.interpreter.IMain
import tools.nsc.{NewLinePrintWriter, Settings}

class ScalaExec {
  val writer = new java.io.StringWriter()
  val settings = new Settings
  settings.usejavacp.value = true
  val interpreter = new IMain(settings, new NewLinePrintWriter(writer, true))

  def run(line: String): String = {
    interpreter.interpret(line)
    val result = writer.getBuffer.toString
    writer.getBuffer.setLength(0)
    result
  }

  def close() = interpreter.close()
}

case class Interpret() extends CommandAgent {

  var exec = new ScalaExec()

  def handle = {
    case CommandData("scala", data) => Some(exec.run(data))

    case CommandData("scala-restart", data) => Some {
      exec.close
      exec = new ScalaExec()
      "Scala restarted successfully"
    }
    case _ => None
  }

}
