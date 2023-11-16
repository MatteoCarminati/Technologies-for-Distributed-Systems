package com.Ex5;

import akka.actor.ActorRef;

public class StartMessage {

    private ActorRef server;

    public StartMessage(ActorRef server){
        this.server = server;
    }

    public ActorRef getServer() {
        return server;
    }
}
