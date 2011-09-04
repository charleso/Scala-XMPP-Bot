package drashid.agent

import akka.actor.ActorRef
import org.jivesoftware.smack.packet.{Message, Presence}
import org.jivesoftware.smack._
import scala.Console
import java.lang.Character

case class XMPPManager(){
  var connection: XMPPConnection = null;

  //Login
  private def login(username: String, password:String , serverType:String): XMPPConnection = {
    val connection = new XMPPConnection(connectionConfig(serverType))
    connection.connect()
    connection.login(username, password)
    return connection
  }

  def connectionConfig(s:String) = s.toLowerCase() match {
    case "gtalk" => new ConnectionConfiguration("talk.google.com", 5222, "gmail.com")
    case _ => null
  }

  def getConnection() = connection

  def setup() = {
    println("Username: ")
    val username = read()
    println("Password: ")
    val pass = readPassword()
    println("Server (options: GTalk): ")
    val server = read()
    connection = login(username, pass, server)
  }

  private def read(): String = {
    return Console.readLine
  }

  private def readPassword(): String = {
    val pass = new jline.ConsoleReader().readLine(new Character('*'))
    return new String(pass)
  }

  def disconnect() {
    connection.disconnect()
  }

  def setupChat(listener: MessageListener) {
    println("Who do you want to chat to?")
    val person = read()
    val chat = connection.getChatManager.createChat(person, listener)
    chat.sendMessage("test")
  }
}

case class ChatSink(chat:Chat, connection:XMPPConnection){
  def output(ans: Any) = {
    val msg = new Message(chat.getParticipant, Message.Type.chat)
    msg.setBody(ans.toString)
    connection.sendPacket(msg)
  }
}

case class XMPPAgentManager(agents:ActorRef*) extends AgentManager(agents: _*) with ChatManagerListener with MessageListener {
  val connectionManager = XMPPManager()
  //Setup Connection
  connectionManager.setup()

  //Listen & Forward Messages
  override def chatCreated(chat:Chat, locally:Boolean){
    chat.addMessageListener(this)
  }
  override def processMessage(chat:Chat, message:Message) {
    self ! (message.getBody, ChatSink(chat, connectionManager.connection))
  }

  //Start Chat
  connectionManager.setupChat(this)

  //Actor shutdown hook
  override def postStop(){
    connectionManager.disconnect()
    println("Disconnected")
  }
}

