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
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import org.cmcc.aero.impl.rpc.GlobalRpcClient.Scale;
import org.cmcc.aero.impl.rpc.message.LocateService;
import org.cmcc.aero.impl.rpc.message.RegisterService;
import org.cmcc.aero.impl.rpc.message.RpcTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by zhuyuqing on 2017/8/1.
 */

public class RpcManager extends AbstractRpcActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final ActorSystem system = context().system();
  private ActorRef mediator;

//  private static String sept = "&&";

  private String selfPath; //akka.tcp://127.0.0.1:5550/user/rpcManager
  private String selfName;
  private String shortPath; ///user/rpcManager

  @Override
  public void preStart() {
    mediator = DistributedPubSub.get(system).mediator();
    mediator.tell(new DistributedPubSubMediator.Put(self()), self());

    Address clusterAddress = Cluster.get(system).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();
    shortPath = self().path().toStringWithoutAddress();
    LOG.info("{} is started.", selfPath);

  }

  @Override
  public void postStop() {
    LOG.info("{} is closing.", selfPath);
  }

  @Override
  public Receive createReceive() {
    return baseReceive().orElse(extendReceive());
  }


  private Receive extendReceive() {
    return receiveBuilder()
      .match(RegisterService.class, regt -> {
        LOG.info("{} got message: {}", selfName, regt);
        String servKey =
//          regt.serviceName + sept +
            regt.serviceType;
        getOrCreateService(servKey).forward(regt, context());
      })
      .match(LocateService.class, loct -> {
        LOG.info("{} got message: {}", selfName, loct);

        String key =
//          loct.serviceName + sept +
            loct.serviceType;
        Optional<ActorRef> service = getRpcService(key);


        if (service.isPresent()) {
          LOG.debug("{} service ifLocal: {}", selfName, service.isPresent());
          service.get().forward(loct, context());
        } else if (loct.scale.equals(Scale.LOCAL)) {
          LOG.debug("{} service local not found.", selfName);
          sender().tell("", ActorRef.noSender());
        }

        LOG.debug("{} service continue.", selfName);

        if (loct.requestAddress == null && loct.scale.equals(Scale.CLUSTER)) {
          LOG.debug("{} service ready to send to mediator: {}", selfName, mediator);
          mediator.tell(new DistributedPubSubMediator.SendToAll(
            shortPath, loct.setRequestAddress(
            selfName), true
          ), sender());
        }

      })
      .match(RpcTask.class, task -> {
        LOG.info("{} got message: {}", selfName, task);

        ActorRef worker = getRpcWorker(task.getTaskId());
        worker.forward(task, getContext());
      })
      .match(DistributedPubSubMediator.SubscribeAck.class, ack -> {
        LOG.info("{} got message: {}", selfName, ack);
      })
      .match(Terminated.class, terminated -> {
        LOG.info("{} got message: {}, died actor: {}", selfName, terminated, terminated.actor().path());
      })
      .build();
  }

  private ActorRef getRpcWorker(String taskId) {
    //TODO add ip information to make workerId unique
    String workerId = taskId;
    String name = "rpcWorker-" + workerId;
    Optional<ActorRef> worker = getContext().findChild(name);
    if (!worker.isPresent()) {

      worker = Optional.of(getContext().actorOf(Props.create(RpcWorker.class), name));
      getContext().watch(worker.get());
    }
    return worker.get();
  }

  private ActorRef getOrCreateService(String serviceKey) {
    String name = "rpcService-" + serviceKey;
    Optional<ActorRef> worker = getContext().findChild(name);
    if (!worker.isPresent()) {
      worker = Optional.of(getContext().actorOf(Props.create(RpcServiceActor.class), name));
      getContext().watch(worker.get());
    }
    return worker.get();
  }


  private Optional<ActorRef> getRpcService(String serviceKey) {
    String name = "rpcService-" + serviceKey;
    return getContext().findChild(name);
  }


}
