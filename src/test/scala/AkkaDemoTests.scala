import akka.actor.testkit.typed.scaladsl.ActorTestKit
import edu.drexel.cs647.{EchoServer,Proxy}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AkkaDemoTests1
    extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers {
  val testKit = ActorTestKit()

  "EchoServer" must {

    "echo a single message" in {
      val es = testKit.spawn(EchoServer(), "echo")
      val probe = testKit.createTestProbe[Proxy.ProxyMessage]()
      es ! EchoServer.Echo("hello", probe.ref)
      probe.expectMessage(Proxy.Ack("hello"))
      testKit.stop(es)
    }

    "repeat a single message" in {
      val es = testKit.spawn(EchoServer(), "echo")
      val probe = testKit.createTestProbe[Proxy.ProxyMessage]()
      es ! EchoServer.Echo("hello", probe.ref)
      probe.expectMessage(Proxy.Ack("hello"))
      es ! EchoServer.Repeat(probe.ref)
      probe.expectMessage(Proxy.Ack("hello"))
      testKit.stop(es)
    }
  }
}