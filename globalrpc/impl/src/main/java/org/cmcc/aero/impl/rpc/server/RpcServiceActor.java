/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;
import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.*;
import org.cmcc.aero.impl.rpc.protocol.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyuqing on 2017/8/7.
 */

public class RpcServiceActor extends UntypedPersistentActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private GlobalRpcIntf service;

  private String selfPath;
  private String selfName;

  private RpcServiceState state = new RpcServiceState();

  @Override
  public void preStart() throws Exception {
    super.preStart();
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();

    LOG.info("{} is started.", selfPath);
  }

  @Override
  public void onReceiveRecover(Object message) throws Exception {
    LOG.info("initial got message: {}", message);

    if(message instanceof RpcTask) {
      state.offer((Event) message);
    } else if (message instanceof RpcTaskCompleted) {
      state.poll(((Event) message).getTaskId());
    } else if (message instanceof SnapshotOffer) {
      state = (RpcServiceState) ((SnapshotOffer) message).snapshot();
    }
  }


  @Override
  public void onReceiveCommand(Object message) throws Exception {
    if (message instanceof RegisterService) {
      RegisterService regt = (RegisterService) message;
      LOG.debug("{} got message: {}", selfName, regt);
      if(service == null && regt.service != null) {
        service = regt.service;
        LOG.debug("initial State:{}", state.toString());
        while(state.getEvents().peek() != null) {
          RpcTask task = (RpcTask) state.getEvents().peek();
          GlobalRpcResult r = GlobalRpcUtils.invoke(service, task.getMethodName(), task.getParameters());
          persist(new RpcTaskCompleted(task.getTaskId()), this::handleRpcTaskEvent);
          ActorSelection select = getContext().actorSelection(task.getClientPath());
          select.tell(r, ActorRef.noSender());
        }
      } else if(service != regt.service) {
        throw new Exception("Error : You have registered unequal service with same serviceType " + service.getServiceType());
      }

      //TODO add reply to client if needed
//        sender().tell(GlobalRpcResult.success(), ActorRef.noSender());
    } else if (message instanceof LocateService) {
      LocateService loct = (LocateService) message;
      LOG.debug("{} got message: {}", selfName, loct);
      if(service != null && service.isResourceLocal(loct.resourceId)) {
        sender().tell(selfPath, self());
      } else if(loct.scale.equals(GlobalRpcClient.Scale.LOCAL)){
        sender().tell("", ActorRef.noSender());
      }else if(service == null){
        LOG.error("{} situation that service is null", selfName);
      } else {
        LOG.debug("{} situation that isResourceLocal=false", selfName);
      }
    } else if (message instanceof RpcTask) {
      RpcTask task = (RpcTask) message;
      //TODO hasn't verify if client path correct
      task.updateClientPath(sender().path().toString());
      LOG.debug("{} got message: {}", selfName, task);

      persist((Event)message, this::handleRpcTaskEvent);

      if (service != null) {
        GlobalRpcResult r = GlobalRpcUtils.invoke(service, task.getMethodName(), task.getParameters());
        sender().tell(r, ActorRef.noSender());
      } else {
        sender().tell(GlobalRpcResult.failure(100302L, "target service is null, it shouldn't happen"), ActorRef.noSender());
      }
//      self().tell(new RpcTaskCompleted(task.getTaskId()), self());
      persist(new RpcTaskCompleted(task.getTaskId()), this::handleRpcTaskEvent);

    } else if (message instanceof RpcTaskCompleted) {
      LOG.info("{} got message: {}", selfName, message);
    }
  }

  private int snapShotInterval = 500;

  private void handleRpcTaskEvent(Event event) throws Exception {
    if(event instanceof RpcTask)
      state.offer(event);
    else if(event instanceof RpcTaskCompleted)
      state.poll(event.getTaskId());

    LOG.debug("handleEvent {} done, and state={}", event, state.copy().toString());
    if(lastSequenceNr() % snapShotInterval == 0 && lastSequenceNr() !=0) {
      saveSnapshot(state.copy());
    }
  }

  @Override
  public String persistenceId() {
    return "rpc-service-persistence-id";
  }
}
