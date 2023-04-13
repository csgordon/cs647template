package edu.drexel.cs647.java;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;

public class Proxy extends AbstractBehavior<ProxyMessage> {
    public static Behavior<ProxyMessage> create(ActorRef<EchoRequest> echo) {
        return Behaviors.setup(context -> {
            return new Proxy(context, echo);
        });
    }

    private ActorRef<EchoRequest> echo;

    // You'll basically always need to implement a constructor to pass the context to the super constructor, even if you don't need fields yourself
    private Proxy(ActorContext ctxt,ActorRef<EchoRequest> echo) {
        super(ctxt);
        this.echo = echo;
    }

    private String lastmsg = null;

    @Override
    public Receive<ProxyMessage> createReceive() {
      // This method is only called once for initial setup
      return newReceiveBuilder()
            // We could register multiple onMessage handlers, for each subclass of ProxyMessage, if we wanted to.
            // By using a single handler for the general message type, it makes it easier to switch handling of all message types simultaneously (in a later project)
          .onMessage(ProxyMessage.class, this::dispatch)
          .build();
    }

    public Behavior<ProxyMessage> dispatch(ProxyMessage msg) {
        // This style of switch statement is technically a preview feature in many versions of Java, so you'll need to compile with --enable-preview
        switch (msg) {
            case ProxyMessage.Request r:
                getContext().getLog().info("[Proxy] requesting echo of: "+r.msg());
                // The flip side of our use of a single dispatch routine, rather than one per message type,
                // is that we need a cast here to work around a Scala/Java interop quirk
                // ... except Java flat out refuses
                echo.tell(new EchoRequest.Echo(r.msg(), getContext().getSelf()));
                break;
            case ProxyMessage.Repeat r:
                getContext().getLog().info("[Proxy] requesting repeat of last message");
                echo.tell(new EchoRequest.Repeat(getContext().getSelf()));
                break;
            case ProxyMessage.Ack a:
                getContext().getLog().info("[Proxy] heard back: "+a.msg());
                break;
            case ProxyMessage.Shutdown s:
                getContext().getLog().info("[Proxy] shutting down");
                return Behaviors.stopped();
        }
        // Keep the same message handling behavior
        return this;
    }
}