package edu.drexel.cs647.java;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import edu.drexel.cs647.java.EchoRequest.Echo;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.ActorRef;

public class EchoServer extends AbstractBehavior<EchoRequest> {
    public static Behavior<EchoRequest> create() {
        return Behaviors.setup(context -> {
            return new EchoServer(context);
        });
    }

    // You'll basically always need to implement a constructor to pass the context to the super constructor, even if you don't need fields yourself
    private EchoServer(ActorContext ctxt) {
        super(ctxt);
    }

    private String lastmsg = null;

    @Override
    public Receive<EchoRequest> createReceive() {
      // This method is only called once for initial setup
      return newReceiveBuilder()
            // We could register multiple onMessage handlers, for each subclass of EchoRequest, if we wanted to.
            // By using a single handler for the general message type, it makes it easier to switch handling of all message types simultaneously (in a later project)
          .onMessage(EchoRequest.class, this::dispatch)
          .build();
    }

    public Behavior<EchoRequest> dispatch(EchoRequest msg) {
        // This style of switch statement is technically a preview feature in many versions of Java, so you'll need to compile with --enable-preview
        switch (msg) {
            case EchoRequest.Echo e:
                lastmsg = e.msg();
                getContext().getLog().info("[EchoServer] echoing "+e.msg());
                e.sender().tell(new ProxyMessage.Ack(lastmsg));
                break;
            case EchoRequest.Repeat r:
                getContext().getLog().info("[EchoServer] repeating "+lastmsg);
                r.sender().tell(new ProxyMessage.Ack(lastmsg));
                break;
            case EchoRequest.End e:
                getContext().getLog().info("[EchoServer] shutting down");
                return Behaviors.stopped();
        }
        // Keep the same message handling behavior
        return this;
    }
}