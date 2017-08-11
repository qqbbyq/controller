/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.dispatch.OnComplete;
import akka.japi.pf.ReceiveBuilder;
import akka.util.Timeout;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.message.RpcTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/8/1.
 */

public class RpcWorker extends AbstractActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Override
  public Receive createReceive(){
    return working();
  }

  private String selfPath;
  private String selfName;//rpcWorker-1@127.0.0.1:5550

  private ActorRef client;

  private Timeout timeout = new Timeout(Duration.create(500, "millis"));

  private GlobalRpcResult taskResult;

  @Override
  public void preStart(){
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();

    LOG.info("{} is started.", selfPath);
  }


  private Receive working(){
    return new ReceiveBuilder()
      .match(RpcTask.class, task -> {
        LOG.info("{} got message: {}", selfName, task);
        client = sender();
        ActorRef self = self();
        ActorSelection selection = getContext().actorSelection(task.getServicePath());
        Future<ActorRef> future = selection.resolveOne(timeout);
        future.onComplete(new OnComplete<ActorRef>() {
          @Override
          public void onComplete(Throwable failure, ActorRef success) throws Throwable {
            if(failure != null) {
              LOG.error("{} got service actor error: {}", selfName, failure.getMessage());
              taskResult = GlobalRpcResult.failure(100301L, failure.getMessage());
              client.tell(taskResult, ActorRef.noSender());
              self.tell(new GoToCompleting(), ActorRef.noSender());
            } else {
              success.tell(task, self);
            }
          }
        }, getContext().getSystem().dispatcher());
        context().become(waiting().onMessage());
      })
      .build();
  }

  private  Receive waiting(){
    return new ReceiveBuilder()
      .match(GlobalRpcResult.class, result -> {
        taskResult = result;
        client.tell(result, ActorRef.noSender());
        //only make sure during one duration, operation is unique, no longer preserve result after that,
        //it means if you submit task with same task id after a duration, your task will be processed
        context().setReceiveTimeout(Duration.create(1, TimeUnit.DAYS));
        context().become(completing().onMessage());
      })
      .match(RpcTask.class, task -> {
        sender().tell(
          GlobalRpcResult.failure(
            100303L,
            "task has been submitted but not completed, please try again later to get the taskResult."
          ), ActorRef.noSender());
      })
      .match(GoToCompleting.class, c -> {
        context().become(completing().onMessage());
      })
      .build();
  }

  private  Receive completing(){
    return new ReceiveBuilder()
      .match(RpcTask.class, task -> {
        sender().tell(taskResult, ActorRef.noSender());
      })
      .match(ReceiveTimeout.class, t -> {
        LOG.info("{} got message: {}", selfName, t);
        context().stop(self());
      })
      .build();
  }

  private static class GoToCompleting {}

}
