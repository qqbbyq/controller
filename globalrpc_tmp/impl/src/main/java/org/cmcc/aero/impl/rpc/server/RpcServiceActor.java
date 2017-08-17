/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.message.LocateService;
import org.cmcc.aero.impl.rpc.message.RegisterService;
import org.cmcc.aero.impl.rpc.message.RpcTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyuqing on 2017/8/7.
 */

public class RpcServiceActor extends UntypedActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private GlobalRpcIntf service;

  private String selfPath;
  private String selfName;

  @Override
  public void preStart(){
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();

    LOG.info("{} is started.", selfPath);
  }

  @Override
  public void onReceive(Object message) {
    LOG.info("{} got message: {} is {}", selfName, message, message.getClass());
    if (message instanceof RegisterService) {
      RegisterService regt = (RegisterService) message;
      LOG.info("{} got message: {}", selfName, regt);
      service = regt.service;
      //TODO add reply to client if needed
//        sender().tell(GlobalRpcResult.success(), ActorRef.noSender());
    } else if (message instanceof LocateService) {
      LocateService loct = (LocateService) message;
      LOG.info("{} got message: {}", selfName, loct);
      if(service != null && service.isResourceLocal(loct.resourceId)) {
        sender().tell(selfPath, ActorRef.noSender());
      } else if(loct.scale.equals(GlobalRpcClient.Scale.LOCAL)){
        sender().tell("", ActorRef.noSender());
      }else if(service == null){
        LOG.error("{} situation that service is null", selfName);
      } else {
        LOG.info("{} situation that isResourceLocal={}", selfName, service.isResourceLocal(loct.resourceId));
      }
    } else if (message instanceof RpcTask) {
      RpcTask task = (RpcTask) message;
      LOG.info("{} got message: {}", selfName, task);
      if (service != null) {
        GlobalRpcResult r = GlobalRpcUtils.invoke(service, task.getMethodName(), task.getParameters());
        sender().tell(r, ActorRef.noSender());
      } else {
        sender().tell(GlobalRpcResult.failure(100302L, "target service is null, it shouldn't happen"), ActorRef.noSender());
      }
    }
  }

}
