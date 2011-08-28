package drashid.agent

import akka.actor.ActorRef
import akka.event.EventHandler
import org.jivesoftware.smack.packet.{Message, Presence}
import org.jivesoftware.smack._
import java.util.Properties
import java.io.FileReader

object ServerConf{
  def config(s:String) = s.toLowerCase() match {
    case "gtalk" => new ConnectionConfiguration("talk.google.com", 5222, "gmail.com")
    case _ => null
  }
}

case class XMPPConfig(username:String, password:String, chatTo:String, serverType:String)

object XMPPConfig{
  def loadFrom(filename:String) = {
    val props = new Properties()
    props.load(new FileReader(filename))
    XMPPConfig(props.getProperty("username"), props.getProperty("password"), props.getProperty("to"), props.getProperty("server"))
  }
}

import ServerConf._
case class XMPPManager(config:XMPPConfig, agents:ActorRef*) extends AgentManager(agents: _*) with ChatManagerListener with MessageListener {
  //Setup
  val connection = new XMPPConnection(ServerConf.config(config.serverType))
  connection.connect()
  connection.login(config.username, config.password)
  connection.sendPacket(new Presence(Presence.Type.available))

  //Listen & Forward Messages
  connection.getChatManager().addChatListener(this)
  override def chatCreated(chat:Chat, locally:Boolean){
    chat.addMessageListener(this)
  }
  override def processMessage(chat:Chat, message:Message){
    self ! message.getBody
  }

  //Actor Receive & Handling
  def process(resp: String) {
    val msg = new Message(config.chatTo, Message.Type.chat)
    msg.setBody(resp)
    connection.sendPacket(msg)
  }

  //Actor shutdown hook
  override def postStop(){
    connection.disconnect()
    println("Connection Closed.")
  }
}

case class ConsoleManager(agents:ActorRef*) extends AgentManager(agents: _*) {
  def process(resp: String) {
    println(resp)
  }
}