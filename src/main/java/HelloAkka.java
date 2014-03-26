import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class HelloAkka {
    public static class Greet implements Serializable {
        public static final long serialVersionUID = 1;
    }

    public static class WhoToGreet implements Serializable {
        public static final long serialVersionUID = 1;
        public final String who;
        public WhoToGreet(String who) {
            this.who = who;
        }
    }

    public static class Greeting implements Serializable {
        public static final long serialVersionUID = 1;
        public final String message;
        public Greeting(String message) {
            this.message = message;
        }
    }

    public static class Greeter extends AbstractActor {
        String greeting = "";

        public Greeter() {
            receive(ReceiveBuilder.
                match(WhoToGreet.class, message -> greeting = "hello, " + message.who).
                match(Greet.class, message -> sender().tell(new Greeting(greeting), self())).
                build());
        }
    }

    public static void main(String[] args) {
        // Create the 'helloakka' actor system
        final ActorSystem system = ActorSystem.create("helloakka");

        // Create the 'greeter' actor
        final ActorRef greeter = system.actorOf(Props.create(Greeter.class), "greeter");

        // Create the "actor-in-a-box"
        final Inbox inbox = Inbox.create(system);

        // Tell the 'greeter' to change its 'greeting' message
        greeter.tell(new WhoToGreet("akka"), ActorRef.noSender());

        // Ask the 'greeter for the latest 'greeting'
        // Reply should go to the "actor-in-a-box"
        inbox.send(greeter, new Greet());

        // Wait 5 seconds for the reply with the 'greeting' message
        Greeting greeting1 = (Greeting) inbox.receive(Duration.create(5, TimeUnit.SECONDS));
        System.out.println("Greeting: " + greeting1.message);

        // Change the greeting and ask for it again
        greeter.tell(new WhoToGreet("typesafe"), ActorRef.noSender());
        inbox.send(greeter, new Greet());
        Greeting greeting2 = (Greeting) inbox.receive(Duration.create(5, TimeUnit.SECONDS));
        System.out.println("Greeting: " + greeting2.message);

        // after zero seconds, send a Greet message every second to the greeter with a sender of the GreetPrinter
        ActorRef greetPrinter = system.actorOf(Props.create(GreetPrinter.class));
        system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), greeter, new Greet(), system.dispatcher(), greetPrinter);
    }

    public static class GreetPrinter extends AbstractActor {
        @Override public PartialFunction<Object, BoxedUnit> receive() {
            return ReceiveBuilder.
                    match(Greeting.class, message -> System.out.println(message.message)).
                    build();
        }
    }
}
