package edu.drexel.cs647.java;
import akka.actor.typed.ActorRef;

// Recent versions of Java have *record* classes: https://docs.oracle.com/en/java/javase/15/language/records.html
// Basically, record classes automatically get private fields to store their constructor arguments, and getter methods (named after the constructor arguement) are automatically generated
public sealed interface EchoRequest {
    /* NOTE: This constructor (and that of Repeat) are different from the
     * Scala version! The direct equivalent of the Scala version here would
     * be to take the return address as ActorRef<ProxyMessage.Ack>.
     * The Scala version does this because that's all the echoserver
     * needs to send to the Proxy (the other ProxyMessage types are for
     * the Orchestrator's use). In Scala that works, because in Scala
     *   ActorRef[ProxyMessage] <: ActorRef[ProxyMessage.Ack]
     * because ActorRef is *contravariant*. Java's generics don't quite
     * interface correctly with Scala's. So we use the more general type
     * everywhere.
     * 
     * Short version: When sending a return address, just use the general
     * type of message, not a specific message type.
     */
    public final record Echo(String msg, ActorRef<ProxyMessage> sender) implements EchoRequest {}
    public final record Repeat(ActorRef<ProxyMessage> sender) implements EchoRequest {}
    public final record End() implements EchoRequest {}
}