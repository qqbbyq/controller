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
import akka.persistence.SnapshotOffer;
import akka.persistence.UntypedPersistentActor;
import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.*;
import org.cmcc.aero.impl.rpc.protocol.Event;
import org.cmcc.aero.impl.rpc.protocol.TaskProtocol;
import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyuqing on 2017/8/7.
 */

public class RpcServiceActor extends UntypedActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private GlobalRpcIntf service;

  private String selfPath;
  private String selfName;

  @Override
  public void preStart() throws Exception {
    super.preStart();
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();

    LOG.info("{} is started.", selfPath);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof RegisterService) {
      RegisterService regt = (RegisterService) message;
      LOG.debug("{} got message: {}", selfName, regt);
      if(service == null && regt.service != null) {
        service = regt.service;
      } else if(service != regt.service) {
        throw new Exception("Error : You have registered unequal service with same serviceType " + service.getServiceType());
      }

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
    } else if(message instanceof RpcTaskHead) {
      RpcTaskHead task = (RpcTaskHead) message;
      LOG.info("got message : TaskProtocol {}", task.taskId);
      getInvoker(task.taskId).forward(message, getContext());

    } else if(message instanceof RpcTaskChunk) {
      RpcTaskChunk task = (RpcTaskChunk) message;
      LOG.info("got message : TaskProtocol {}", task.taskId);
      getInvoker(task.taskId).forward(message, getContext());

    }
  }

  private ActorRef getInvoker(String taskId) {
    String name = "invoker-" + taskId;
    ActorRef invoker = getContext().getChild(name);
    if (invoker == null) {
      invoker = getContext().actorOf(Props.create(Invoker.class, service), name);
      getContext().watch(invoker);
    }
    return invoker;
  }


//  static class InvokeTask{
//    public GlobalRpcIntf service;
//    public String methodName;
//    public Object[] parameters;
//
//    public InvokeTask(GlobalRpcIntf service, String methodName, Object[] parameters) {
//      this.service = service;
//      this.methodName = methodName;
//      this.parameters = parameters;
//    }
//  }

}
