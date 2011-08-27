package drashid.agent

import akka.actor.Actor
import scala.collection.JavaConversions._
import akka.event.EventHandler
import akka.actor.Actor._

case class Msg(content: String)

case class AgentManager(agents: Agent*) extends Actor {
  val Command = """(![^\s]+)\s+(.*)""".r

  def receive = {
    case 'stop => stop()
    case Msg(Command(command, content)) => delegate(command, content)
    case _ =>
  }

  def stop(){
    this.self.linkedActors.foreach(_._2.stop())
    this.self.stop()
  }

  def delegate(content: String){
    println("Command: " + command + " content: " + content)
  }
}

class Agent {

}

object Main{

  def main(args: Array[String]){
    val actor = actorOf[AgentManager].start()
    actor ! "test"
    actor ! "message"

    println("Press any key to exit...")
    System.in.read();
    Actor.registry.shutdownAll()
  }

}