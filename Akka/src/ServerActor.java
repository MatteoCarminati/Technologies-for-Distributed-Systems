package com.Ex5;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ServerActor extends AbstractActor {

    private Map<String,String> contactList = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PutMsg.class, this::putMsg)
                .match(GetMsg.class,this::getMsg)
                .build();
    }

    void putMsg(PutMsg msg) throws Exception {
        if(msg.getName().equals("Fail")){
            System.out.println("We have lost..."+contactList.entrySet().size()+" messages stored");
            throw new Exception("Server fault");
        } else {
            this.contactList.put(msg.getMail(),msg.getName());
            System.out.println("inserted " + msg.getMail() + ":" + msg.getName());
        }
    }
    
    @Override
    public void preRestart(Throwable reason, Optional<Object> message) { 
        System.out.print("Preparing to restart...");
    }

    @Override
    public void postRestart(Throwable reason){
        System.out.println("Restarted");
        System.out.println("We have recovered..."+contactList.entrySet().size()+" messages");
    }

    void getMsg(GetMsg msg) {
        String res = contactList.containsKey(msg.getName())?contactList.get(msg.getName()):"Name not found";
        sender().tell(new ReplyMsg(res),self());
    }

    static Props props() {
		return Props.create(ServerActor.class);
	}
}