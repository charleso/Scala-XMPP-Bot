package drashid.agent

import scala.collection.JavaConversions._
import akka.actor.Actor._
import akka.actor.{ActorRef, Actor}

/**
 * Message Data Types
 */
case class Request(cmd: String, data: String, parent: ActorRef){
  val command = Command(cmd, data)
}
case class Command(command: String, data: String)
case class Response(ans: Option[String])

/**
 *  Agent Supervisor
 */
abstract class AgentManager(agents: ActorRef*) extends Actor {
  agents.foreach( self.link(_) )
  agents.foreach( _.start() )

  val CommandPattern = """^!([^\s]+)\s*(.*)$""".r
  def receive = {
    case 'stop => stop()
    case CommandPattern(command, data) => delegate(Request(command.toLowerCase(), data, self))
    case Response(Some(ans)) => process(ans)
    case _ =>
  }

  def stop(){
    self.linkedActors.foreach(_._2.stop())
    self.stop()
  }

  def delegate(req: Any){
    agents.foreach( _ ! req )
  }

  def process(ans:String)
}

/**
 * Agent Base
 */
abstract class Agent extends Actor {
  def receive = {
    case req:Request => req.parent ! Response(handle.apply(req command))
    case _ =>
  }

  def handle: PartialFunction[Command, Option[String]]
}

object Main{

  def main(args: Array[String]){
    val google = actorOf(Google())
    val umbrella = actorOf(Umbrella())
    val greet = actorOf(Greet())
    val manager = actorOf(new XMPPManager(XMPPConfig.loadFrom("/Users/drashid/github/scalabot/src/main/resources/xmpp.conf"), umbrella, google, greet)).start()
    manager ! "!greet"

    println("Press any key to stop.")
    System.in.read()
    manager ! 'stop
  }

}