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
import akka.japi.Procedure;
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

public class RpcWorker extends UntypedActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Override
  public void onReceive(Object message) throws Exception {
    working.apply(message);
  }

  private String selfPath;
  private String selfName;//rpcWorker-1@127.0.0.1:5550

  private ActorRef client;

  private Timeout timeout = new Timeout(Duration.create(500, "millis"));

  private GlobalRpcResult taskResult;

  Procedure<Object> working = new Procedure<Object>() {
    @Override
    public void apply(Object message) {
      if(message instanceof RpcTask) {
        RpcTask task = (RpcTask) message;
        LOG.info("{} got message 19: {}", selfName, task);

        try {
          client = sender();
          ActorRef self = self();
          ActorSelection selection = getContext().actorSelection(task.getServicePath());
          selection.tell(task, self);

//        Future<ActorRef> future = selection.resolveOne(timeout);
//        future.onComplete(new OnComplete<ActorRef>() {
//          @Override
//          public void onComplete(Throwable failure, ActorRef success) throws Throwable {
//            if(failure != null) {
//              LOG.error("{} got service actor error: {}", selfName, failure.getMessage());
//              taskResult = GlobalRpcResult.failure(100301L, failure.getMessage());
//              client.tell(taskResult, ActorRef.noSender());
//              self.tell(new GoToCompleting(), ActorRef.noSender());
//            } else {
//              LOG.info("{} got service actor: {}", selfName, success.path());
//              getContext().actorSelection(success.path()).tell(task, self);
//            }
//          }
//        }, getContext().system().dispatcher());
          getContext().become(waiting);

        } catch (Exception e) {
          LOG.error("working RpcTask error {}", e.getMessage());
          e.printStackTrace();
          sender().tell( GlobalRpcResult.failure(
            100304L,
            "error send task to selection servicePath."
          ), ActorRef.noSender());
        }

      } else {
        unhandled(message);
      }
    }
  };

  private Procedure<Object> waiting = new Procedure<Object>() {
    @Override
    public void apply(Object message) {
      LOG.info("{} waiting got message: {}", selfName, message);

      if(message instanceof GlobalRpcResult) {
        GlobalRpcResult result = (GlobalRpcResult) message;
        taskResult = result;
        client.tell(result, ActorRef.noSender());
        //only make sure during one duration, operation is unique, no longer preserve result after that,
        //it means if you submit task with same task id after a duration, your task will be processed
        context().setReceiveTimeout(Duration.create(1, TimeUnit.DAYS));
        getContext().become(completing);
      } else if(message instanceof RpcTask) {
        RpcTask task = (RpcTask) message;
        sender().tell(
          GlobalRpcResult.failure(
            100303L,
            "task has been submitted but not completed, please try again later to get the taskResult."
          ), ActorRef.noSender());
      } else if(message instanceof GoToCompleting) {
        getContext().become(completing);
      } else {
        unhandled(message);
      }
    }
  };

  @Override
  public void preStart(){
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();

    LOG.info("{} is started.", selfPath);
  }


  private Procedure<Object> completing = new Procedure<Object> (){

    @Override
    public void apply(Object message) {
      if (message instanceof RpcTask) {
        sender().tell(taskResult, ActorRef.noSender() );
      } else if (message instanceof ReceiveTimeout) {
        LOG.info("{} got message: {}", selfName, message);
        context().stop(self());
      } else {
        unhandled(message);
      }
    }

  };

  private static class GoToCompleting {}

}
