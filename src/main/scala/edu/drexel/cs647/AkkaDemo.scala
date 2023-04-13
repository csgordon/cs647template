package edu.drexel.cs647

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.io.StdIn.readLine

// In Scala, in addition to classes there are also "companion objects"
// They are essentially built-in support for the singleton pattern
// You can normally have both a class and a companion object with the
// same name. Usually the companion object is used to group various
// helper definitions for the class, but if you really just need one
// of something, you can just use it like a single object.
object EchoServer {
    /* An [ActorRef] is a reference to another actor, which is how we send messages
       to another actor. The type parameter specifies the type of message we can send to
       the recipient.
       Note that ActorRefs are /contravariant/: if T extends U, then ActorRef[U] is a subtype of ActorRef[T]
       (because an actor that can handle any U can naturally handle any T)
       Scala uses /case classes/ for simple records.
       This is analagous to a small inheritance hierarchy whose classes just store some data, with no interesting methods (that's basically what this compiles into).
       If you're familiar with functional programming languages, this is the Scala equivalent of an algebraic datatype.

       Akka messages, if they require a response, must include a "return address" (here, [sender])
    */
    sealed abstract class EchoRequest(val sender: ActorRef[Proxy.Ack])
    final case class Echo(val msg: String, override val sender: ActorRef[Proxy.Ack]) extends EchoRequest(sender)
    final case class Repeat(override val sender: ActorRef[Proxy.Ack]) extends EchoRequest(sender)
    final case class End(override val sender: ActorRef[Proxy.Ack]) extends EchoRequest(sender)

    // The [apply()] is called when this is used as a function with 0 arguments.
    // So [EchoServer()] will call this method
    def apply() : Behavior[EchoServer.EchoRequest] =
        // Behaviors.setup allows executing some code when an actor is first
        // created. In this case, we're just setting up a mutable variable
        // [lastMsg] to remember things for repeats.
        //
        // Formally, lastMsg is a mutable variable captured in a closure, and therefore
        // accessible to each branch of the match.
        //
        // This actually gets compiled down to a field of an anonymous class on the JVM.
        // You can also just define this in a real class instead of a companion object,
        // and just use the field as you'd normally expect coming from Java.
        Behaviors.setup { context =>
            var lastMsg = ""
            Behaviors.receive { (context, msg) =>
                msg match {
                    // Scala allows a sort of type-case construct here
                    // This first clause matches if [msg] is an [Echo],
                    // in which case [e] is bound to hold the same value as
                    // [msg], but at the more specific type (allowing us
                    // to access the field that's only in that subclass)
                    // Java has a similar feature now (https://blogs.oracle.com/javamagazine/post/java-pattern-matching-switch-preview)
                    case e:EchoServer.Echo => 
                        lastMsg = e.msg
                        context.log.info("EchoServer echoing {}", e.msg)
                        e.sender ! Proxy.Ack(lastMsg)
                        // This is a special token behavior for "don't change what you're doing"
                        Behaviors.same
                    case r:EchoServer.Repeat => 
                        context.log.info("EchoServer repeating {}", lastMsg)
                        r.sender ! Proxy.Ack(lastMsg)
                        Behaviors.same
                    case e:EchoServer.End => 
                        context.log.info("EchoServer shutting down")
                        // This is a behavior token for "shut down this actor."
                        // This is the proper way to shut down an actor when 
                        // nothing has gone catastrophically wrong.
                        Behaviors.stopped
                }
            }
        }
}

object Proxy {
    sealed abstract class ProxyMessage
    final case class Ack(msg: String) extends ProxyMessage
    final case class Repeat() extends ProxyMessage
    final case class Request(msg: String) extends ProxyMessage
    final case class Shutdown() extends ProxyMessage
}

class Proxy(val dest: ActorRef[EchoServer.EchoRequest]) {
    def apply() : Behavior[Proxy.ProxyMessage] =
        Behaviors.receive { (context, msg) =>
            msg match {
                case Proxy.Request(msg) =>
                    context.log.info("[Proxy] requesting echo of: {}", msg)
                    dest ! EchoServer.Echo(msg, context.self)
                    Behaviors.same
                // The underscore means "I don't care what this is called"
                case _:Proxy.Repeat =>
                    context.log.info("[Proxy] requesting repeat of last message")
                    // Notice we're using contravariance here: the [Repeat]
                    // message type takes an actor reference accepting only Acks,
                    // but we're passing one that handles more than just acks.
                    dest ! EchoServer.Repeat(context.self)
                    Behaviors.same
                case Proxy.Ack(msg) =>
                    context.log.info("[Proxy] Heard back: {}", msg)
                    Behaviors.same
                case _:Proxy.Shutdown =>
                    context.log.info("[Proxy] shutting down")
                    Behaviors.stopped
            }
        }
}

sealed class ShutdownMessage {}

object Orchestrator {
    def apply(): Behavior[Any] =
        Behaviors.setup { context =>
            val echo = context.spawn(EchoServer(), "echoserver")
            val proxy = context.spawn(Proxy(echo)(), "proxyserver")
            Behaviors.receive { (context,message) =>
                context.log.info(s"[Orchestrator] received '$message'")
                message match {
                    case "repeat" =>
                        proxy ! Proxy.Repeat()
                        Behaviors.same
                    case s:String =>
                        proxy ! Proxy.Request(s)
                        Behaviors.same
                    case _: ShutdownMessage =>
                        echo ! EchoServer.End(context.self)
                        proxy ! Proxy.Shutdown()
                        Behaviors.stopped

                }
            }
        }
}

object Program extends App {
    val orc = ActorSystem(Orchestrator(), "AkkaDemo1")

    var done = false
    while (!done) {
        val command = readLine()
        command match {
            case "shutdown" =>
                orc ! ShutdownMessage()
                orc.terminate()
                done = true
            case m: String =>
                orc ! m
        }
    }

}