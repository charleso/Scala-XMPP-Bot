package drashid.agent

case class Login(username: String, password: String, serverType: String, nick: String)

trait Config {
  def setup: Login

  def person: String
}

class ConfigBean(val setup: Login, val person: String) extends Config

class ArgsConfig(args: Array[String]) extends Config {

  if (args.size != 5) {
    sys.error("Usage: username password server nickname group")
  }

  def setup() = Login(args(0), args(1), args(2), args(3))

  def person() = args(4)

}

object StdinConfig extends Config {

  override def setup() = {
    println("Username: ")
    val username = read()
    println("Password: ")
    val pass = read() //TODO: password masking on CL
    println("Server (options: GTalk): ")
    val server = read()
    println("Nick: ")
    val nick = read()
    Login(username, pass, server, nick)
  }

  override def person() = {
    println("Who do you want to chat to?")
    read()
  }

  private def read(): String = Console.readLine

}