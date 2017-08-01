package org.opendaylight.controller.rpc;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Created by zhuyuqing on 2017/8/1.
 */

public class RpcClientManager extends UntypedActor{

  ClusterClientReceptionist.get(system).registerService(serviceB);

  private ActorRef getRpcWorker(long id) {
    String name = "RpcWorker-" + id;
    ActorRef worker = getContext().getChild(name);
    if (worker == null) {
      worker = getContext().actorOf(Props.create(RpcWorker.class, id), name);
      getContext().watch(worker);
    }
    return worker;
  }

  @Override
  public void onReceive(Object message){
    if(message instanceof RpcTask) {
      RpcTask task = (RpcTask) message;
      ActorRef worker = getRpcWorker(task.getId());
      worker.forward(message, getContext());
    }
  }




}
