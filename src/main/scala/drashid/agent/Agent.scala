package drashid.agent

import scala.collection.JavaConversions._
import akka.actor.Actor._
import akka.actor.{ActorRef, Actor}

case class Command(command: String, data: String, parent: ActorRef)
case class Answer(ans: Option[String])

class AgentManager(agents: ActorRef*) extends Actor {
  agents.foreach( self.link(_))
  agents.foreach( _.start() )

  val CommandPattern = """^!([^\s]+)\s+(.*)$""".r
  def receive = {
    case 'stop => stop()
    case CommandPattern(command, data) => delegate(Command(command, data, self))
    case Answer(ans) => println(ans)
    case _ =>
  }

  def stop(){
    self.linkedActors.foreach(_._2.stop())
    self.stop()
  }

  def delegate(command: Command){
    agents.foreach( _ ! command )
  }
}

abstract class Agent extends Actor {
  def receive = {
    case cmd:Command => cmd.parent ! Answer(handle.apply(cmd))
    case _ =>
  }

  def handle: PartialFunction[Command, Option[String]]
}

case class Google() extends Agent{
  def handle = {
    case Command("google", data, _) => Some("http://lmgtfy.com/?q=" + normalize(data))
    case _ => None
  }
  def normalize(data: String) = data.replaceAll(" ", "+")
}

object Main{

  def main(args: Array[String]){
    val google = actorOf(Google())
    val manager = actorOf(new AgentManager(google)).start()
    manager ! "!google Lookup something I want to know about"
    manager ! 'stop
  }

}