package drashid.agent

import scala.collection.JavaConversions._
import akka.actor.Actor._
import akka.actor.{ActorRef, Actor}

/**
 * Data Exchange Classes
 */
trait Sender{
  val parent:ActorRef;
}
case class AgentRequest(data:Any, override val parent: ActorRef) extends Sender
case class AgentResponse(ans: Option[String])

case class CommandData(command: String, data: String)
case class FreeTextData(text:String)

/**
 *  Agent Supervisor
 */
abstract class AgentManager(agents: ActorRef*) extends Actor {
  agents.foreach( self.link(_) )
  agents.foreach( _.start() )

  val CommandPattern = """^!([^\s]+)\s*(.*)$""".r
  def receive = {
    case 'stop => stop()
    case CommandPattern(command, commandData) => delegate(AgentRequest(CommandData(command.toLowerCase(), commandData), self))
    case text:String => delegate(AgentRequest(FreeTextData(text), self))
    case AgentResponse(Some(ans)) => process(ans)
    case _ =>
  }

  def stop(){
    self.linkedActors.foreach(_._2.stop())
    self.stop()
  }

  def delegate(req: AgentRequest){
    agents.foreach( _ ! req )
  }

  def process(ans:String)
}

/**
 * Agent Base
 */
abstract class Agent extends Actor {
  def receive = {
    case req:AgentRequest => req.parent ! AgentResponse(handle.apply(req data))
    case _ =>
  }

  def handle: PartialFunction[Any, Option[String]]
}
