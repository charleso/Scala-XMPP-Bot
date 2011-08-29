package drashid.agent

import akka.actor.Actor._

/**
 * XMPP Runner
 */
object Main{

  def main(args: Array[String]){
    var xmppConf = "src/main/resources/xmpp.conf"
    if(args.length >= 1){
      xmppConf = args(0)
    }
    val google = actorOf(Google())
    val umbrella = actorOf(Umbrella())
    val greet = actorOf(Greet())
    val manager = actorOf(new XMPPManager(XMPPConfig.loadFrom(xmppConf), umbrella, google, greet)).start()
    manager ! "!greet"

    println("Press any key to stop.")
    System.in.read()
    manager ! 'stop
  }

}