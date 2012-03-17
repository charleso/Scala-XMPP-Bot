package drashid.agent

import akka.actor.Actor._

/**
 * XMPP Runner
 */
object Main{

  def main(args: Array[String]){
    val scalex = actorOf(Scalex())
    val ls = actorOf(Ls())
    val interpret = actorOf(Interpret())

    implicit val config = new ArgsConfig(args)
    val manager = actorOf(new XMPPAgentManager(scalex, ls, interpret)).start()

    println("Press any key to stop.")
    System.in.read()
    manager ! 'stop
  }

}