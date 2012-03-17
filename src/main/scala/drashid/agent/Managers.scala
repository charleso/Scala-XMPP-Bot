package drashid.agent

import akka.actor.ActorRef
import org.jivesoftware.smack._
import packet.{Packet, Message}
import org.jivesoftware.smackx.muc.{DiscussionHistory, MultiUserChat}

case class ChatSink(chat: Chat, connection: XMPPConnection) extends Sink {
  override def output(ans: Any) = {
    val msg = new Message(chat.getParticipant, Message.Type.chat)
    msg.setBody(ans.toString)
    connection.sendPacket(msg)
  }
}

case class GroupChatSink(chat: MultiUserChat) extends Sink {
  override def output(ans: Any) = {
    chat.sendMessage(ans.toString)
  }
}

case class XMPPAgentManager(agents: ActorRef*)(implicit config: Config) extends AgentManager(agents: _*) with ChatManagerListener with MessageListener {

  val login = config.setup

  import login._

  val connection = new XMPPConnection(serverType)
  connection.connect()
  connection.login(username, password)

  connection.getChatManager.addChatListener(this)

  val person = config.person
  val chat = new MultiUserChat(connection, person)
  val history: DiscussionHistory = new DiscussionHistory()
  history.setMaxStanzas(0)
  chat.join(nick, null, history, SmackConfiguration.getPacketReplyTimeout())

  chat.addMessageListener(new PacketListener() {
    def processPacket(packet: Packet) {
      packet match {
        case m: Message => {
          self !(m.getBody, GroupChatSink(chat))
        }
      }
    }
  })

  //Listen & Forward Messages
  override def chatCreated(chat: Chat, locally: Boolean) {
    chat.addMessageListener(this)
  }

  override def processMessage(chat: Chat, message: Message) {
    self !(message.getBody, ChatSink(chat, connection))
  }

  //Actor shutdown hook
  override def postStop() {
    connection.disconnect()
    println("Disconnected.")
  }
}


