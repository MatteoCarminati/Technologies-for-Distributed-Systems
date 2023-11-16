package com.Ex5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import static akka.pattern.Patterns.ask;

public class Main {
	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef serverSupervisor = sys.actorOf(Supervisor.props(), "supervisor");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");

	    ActorRef server;
        try {
            scala.concurrent.Future<Object> waiting = ask(serverSupervisor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waiting.result(scala.concurrent.duration.Duration.create(5,SECONDS), null);
            final ExecutorService exec = Executors.newFixedThreadPool(1);
            exec.submit(() -> client.tell(new StartMessage(server), ActorRef.noSender()));
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
