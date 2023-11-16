package com.Ex5;

import java.time.Duration;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class Supervisor extends AbstractActor{

    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            10, // Max no of retries
            Duration.ofMinutes(2), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart())
                .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public Supervisor(){}

    @Override
    public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          .match(
		              Props.class,
		              props -> {
		                getSender().tell(getContext().actorOf(props), getSelf());
		              })
		          .build();
	}
    
    static Props props() {
		return Props.create(Supervisor.class);
	}

}
