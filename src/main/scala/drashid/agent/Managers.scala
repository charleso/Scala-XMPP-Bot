package drashid.agent

import akka.actor.ActorRef
import org.jivesoftware.smack.packet.{Message}
import org.jivesoftware.smack._
import scala.Console

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
    val pass = read()  //TODO: password masking on CL
    println("Server (options: GTalk): ")
    val server = read()
    connection = login(username, pass, server)
  }

  private def read(): String = {
    return Console.readLine
  }

  def disconnect() {
    connection.disconnect()
  }

  //TODO: group chat option
  def setupChat(listener: ChatManagerListener with MessageListener) {
    connection.getChatManager.addChatListener(listener)
    println("Who do you want to chat to?")
    val person = read()
    val chat = connection.getChatManager.createChat(person, listener)
    chat.sendMessage("Hi, I'm a bot.")
  }
}

case class ChatSink(chat:Chat, connection:XMPPConnection) extends Sink{
  override def output(ans: Any) = {
    val msg = new Message(chat.getParticipant, Message.Type.chat)
    msg.setBody(ans.toString)
    connection.sendPacket(msg)
  }
}

case class XMPPAgentManager(agents:ActorRef*) extends AgentManager(agents: _*) with ChatManagerListener with MessageListener {
  val connectionManager = XMPPManager()
  //Setup Connection
  connectionManager.setup()
  //Start Chat
  connectionManager.setupChat(this)

  //Listen & Forward Messages
  override def chatCreated(chat:Chat, locally:Boolean){
    chat.addMessageListener(this)
  }
  override def processMessage(chat:Chat, message:Message) {
    self ! (message.getBody, ChatSink(chat, connectionManager.connection))
  }

  //Actor shutdown hook
  override def postStop(){
    connectionManager.disconnect()
    println("Disconnected.")
  }
}

