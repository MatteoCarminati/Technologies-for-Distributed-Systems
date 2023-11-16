package com.Ex5;

import java.util.Random;
import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ClientActor extends AbstractActor {

    Random r = new Random();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(StartMessage.class,this::start)
            .build();
    } 

    static Props props() {
        return Props.create(ClientActor.class);
    }
  
    public void start(StartMessage msg) {
        ActorRef server = msg.getServer();
        int i = 0;
        boolean fail = false;
        while (i<40) {
            if(r.nextInt(10)<7){
                if(r.nextInt(10)<=2){
                    fail = true;
                    server.tell(new PutMsg("Fail","Fail"),self());
                } else {
                    server.tell(new PutMsg(String.valueOf((int) (r.nextInt(10))), String.valueOf((int) (r.nextInt(10)))),self());
                }
                
            } else {
                Future<Object> future = Patterns.ask(server,new GetMsg(String.valueOf((int) (r.nextInt(10)))),5000);
                ReplyMsg res;
                try {
                    res = (ReplyMsg) Await.result(future,Duration.create(5000,"ms"));
                    System.out.println(res.getEmail());
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }            
        }
        System.out.println("finsihed");
    }
}
