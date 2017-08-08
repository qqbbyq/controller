package org.cmcc.aero.impl.rpc.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.japi.pf.ReceiveBuilder;
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

public class RpcServiceActor extends AbstractActor {

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
  public Receive createReceive() {
    return new ReceiveBuilder()
      .match(RegisterService.class, regt -> {
        LOG.info("{} got message: {}", selfName, regt);
        service = regt.service;
        //TODO add reply to client if needed
//        sender().tell(GlobalRpcResult.success(), ActorRef.noSender());
      })
      .match(LocateService.class, loct -> {
        LOG.info("{} got message: {}", selfName, loct);
        if(service != null && service.isResourceLocal(loct.resourceId)) {
          sender().tell(selfPath, ActorRef.noSender());
        } else if(loct.scale.equals(GlobalRpcClient.Scale.LOCAL)){
          sender().tell("", ActorRef.noSender());
        }
      })
      .match(RpcTask.class, task -> {
        LOG.info("{} got message: {}", selfName, task);
        if (service != null) {
          GlobalRpcResult r = GlobalRpcUtils.invoke(service, task.getMethodName(), task.getParameters());
          sender().tell(r, ActorRef.noSender());
        } else {
          sender().tell(GlobalRpcResult.failure(100302L, "target service is null, it shouldn't happen"), ActorRef.noSender());
        }

      })
      .build();
  }

}
