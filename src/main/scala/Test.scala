import akka.actor.Actor
import akka.actor.Actor._
import akka.event.EventHandler

object Main{

  class MyActor extends Actor {
    def receive = {
      case "test" => EventHandler.info(this, "received test")
      case _ => EventHandler.info(this, "received unknown message")
    }
  }

  def main(args: Array[String]){
    val actor = actorOf[MyActor].start()
    actor ! "test"
    actor ! "message"
    Thread.sleep(1000)

    Actor.registry.shutdownAll()
  }

}