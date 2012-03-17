package drashid.agent

import scala.collection.JavaConversions._
import akka.actor.{ActorRef, Actor}

/**
 * Data Exchange Classes
 */
case class AgentRequest(data:Any, parent: ActorRef, requester:Sink)
case class AgentResponse(ans: Option[Any], requester:Sink)
case class MessageData(data:Any)

/**
 *  Agent Supervisor
 */
abstract class AgentManager(agents: ActorRef*) extends Actor {
  agents.foreach( self.link(_) )
  agents.foreach( _.start() )

  def receive = {
    case 'stop => stop()
    case AgentResponse(Some(ans), sink) => sink.output(ans)
    case AgentResponse(None, _) => //pass
    case (data, sink: Sink) => delegate(AgentRequest(MessageData(data), self, sink))
    case data => delegate(AgentRequest(MessageData(data), self, defaultSink))
  }

  def stop(){
    self.linkedActors.foreach(_._2.stop())
    self.stop()
  }

  def delegate(req: AgentRequest){
    agents.foreach( _ ! req )
  }

  def defaultSink(): Sink = ConsoleSink()

}

/**
 * Output Sinks
 */
trait Sink{
  def output(answer: Any)
}
case class ConsoleSink() extends Sink{
  def output(ans: Any) = {
    println( ans )
  }
}

/**
 * Agent Base
 */
abstract class Agent extends Actor {
  def receive = {
    case AgentRequest(data, parent, sink) => send(AgentResponse(handle.apply(data), sink), parent)
    case _ =>
  }

  def send(message:AgentResponse, parent:ActorRef) { message match {
      case AgentResponse(None, _) => //filter unanswered responses
      case answer => parent ! answer
    }
  }

  def handle: PartialFunction[Any, Option[Any]]
}

/**
 * Agent Base that additionally pre-parses messages that follow the pattern:
 *   !COMMAND EXTRA STUFF HERE
 *   Example: !google something I want to know about
 */
case class CommandData(command: String, data: String)

abstract class CommandAgent extends Agent {
  val CommandPattern = """(?s)^!([^\s]+)\s*(.*)$""".r
  override def receive = {
    case AgentRequest(MessageData(CommandPattern(command, commandData)), parent, sink) =>
      parent ! AgentResponse(handle.apply(CommandData(command.toLowerCase(), commandData)), sink)
    case x => super.receive.apply(x)
  }
}