package org.cmcc.aero.impl.rpc.server;

import akka.actor.*;
import akka.cluster.Cluster;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.message.RpcTaskChunk;
import org.cmcc.aero.impl.rpc.message.RpcTaskChunkReply;
import org.cmcc.aero.impl.rpc.message.RpcTaskHead;
import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/9/1.
 */

public class Invoker extends UntypedActor {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private String selfPath;
  private String selfName;

  private GlobalRpcIntf service;

  Object[] generateTaskParameters(byte[] contents, List<Class<?>> parametersTypes) {
    Object[] parameters = new Object[parametersTypes.size()];
    try (
      ByteArrayInputStream bis = new ByteArrayInputStream(contents);
      ObjectInputStream ois = new ObjectInputStream(bis)
    ) {
      LOG.info("generateTaskParameters byte length={}", contents.length);
      for(int i = 0; i < parametersTypes.size(); ++i) {
        if(parametersTypes.get(i).equals(NormalizedNodeMessages.Node.class) ) {
          byte[] bs = (byte []) ois.readObject();
          parameters[i] = NormalizedNodeMessages.Node.parseFrom(bs);
        } else if (parametersTypes.get(i).equals(NormalizedNodeMessages.InstanceIdentifier.class) ){
          byte[] bs = (byte []) ois.readObject();
          parameters[i] = NormalizedNodeMessages.InstanceIdentifier.parseFrom(bs);
        } else {
          parameters[i] = ois.readObject();
        }
      }
      return parameters;

    } catch (Exception e) {
      LOG.error("generateTaskParameters error:{}", e);
      return null;
    }
  }

  public GlobalRpcResult invokeMethod(Object[] parameters) {
    GlobalRpcResult r;
    if (this.service != null) {
      r = GlobalRpcUtils.invoke(this.service, head.methodName, parameters);
    } else {
      r = GlobalRpcResult.failure(100302L, "target service is null, it shouldn't happen");
    }
    return r;
  }

  public Invoker(GlobalRpcIntf service) {
    this.service = service;
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    Address clusterAddress = Cluster.get(getContext().system()).selfAddress();
    selfPath = self().path().toStringWithAddress(clusterAddress);
    selfName = self().path().name() + "@" + clusterAddress.host().get() + ":" + clusterAddress.port().get();
    context().setReceiveTimeout(Duration.create(1, TimeUnit.HOURS));
    LOG.debug("{} is starting", self().path());

  }

   @Override
  public void postStop() {
    LOG.debug("{} is stopping", self().path());
  }


  private RpcTaskHead head;

  @Override
  public void onReceive(Object message) throws Exception {
    if(message instanceof RpcTaskHead) {

      this.head = (RpcTaskHead) message;

      LOG.info("{} got message: {}", selfName, head);
      sender().tell(new RpcTaskChunkReply(head.taskId, 1), self());

      if(head.totalChunkIndex <= 1) {
        Object[] parameters = generateTaskParameters(head.contents, head.parameterTypes);
        GlobalRpcResult r = invokeMethod(parameters);
        sender().tell(r, ActorRef.noSender());
        context().stop(self());
      }

    } else if(message instanceof RpcTaskChunk) {
      RpcTaskChunk chunk = (RpcTaskChunk) message;
      LOG.info("{} got message: {}", selfName, chunk);

      byte[] tmp = new byte[head.contents.length + chunk.contents.length];
      System.arraycopy(head.contents, 0, tmp, 0, head.contents.length);
      System.arraycopy(chunk.contents, 0, tmp, head.contents.length, chunk.contents.length);

      sender().tell(new RpcTaskChunkReply(head.taskId, chunk.chunkIndex), self());

      if(chunk.chunkIndex == head.totalChunkIndex) {
        Object[] parameters = generateTaskParameters(tmp, head.parameterTypes);
        GlobalRpcResult r = invokeMethod(parameters);
        sender().tell(r, ActorRef.noSender());
        context().stop(self());
      } else {
        head.contents = tmp;
      }

    } else if(message instanceof ReceiveTimeout) {
      context().stop(self());
    } else {
      unhandled(message);
    }
  }
}
