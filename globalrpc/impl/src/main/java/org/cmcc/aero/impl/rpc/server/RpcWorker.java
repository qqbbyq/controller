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
import akka.japi.Procedure;
import akka.util.Timeout;
import org.cmcc.aero.impl.rpc.message.*;
import org.opendaylight.controller.cluster.datastore.utils.SerializationUtils;
import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  private final int baseChunkSize = 1 * 1024 * 1024; //1m

  private RpcTaskHead head ;

  private byte[] state;
  private int startByteIndex = 0;
  private int lastChunkIndex = 1;
  private int totalChunkIndex;


  RpcTaskHead generateTaskHead(RpcTask task) {
    try(
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
    ) {
      List<Class<?>> parameterTypes = new ArrayList<>();
      for(int i = 0 ; i < task.getParameters().length; ++i) {
          parameterTypes.add(task.getParameters()[i].getClass());
          Object para = task.getParameters()[i];
          if(para instanceof NormalizedNodeMessages.Node) {
            byte[] bs = ((NormalizedNodeMessages.Node) para).toByteArray();
            oos.writeObject(bs);
          } else if(para instanceof NormalizedNodeMessages.InstanceIdentifier) {
            LOG.info("generateTaskHead para, {}", para.getClass());
            byte[] bs = ((NormalizedNodeMessages.InstanceIdentifier) para).toByteArray();
            oos.writeObject(bs);
          } else {
            oos.writeObject(para);
          }
      }
      state = bos.toByteArray();
//      for (byte b: state) {
//        System.out.print(b);
//      }
//      System.out.println("generateTaskHead……");

      totalChunkIndex = state.length % baseChunkSize == 0 ? state.length / baseChunkSize : state.length / baseChunkSize + 1;
      int size;
      if(state.length > baseChunkSize) {
        size = baseChunkSize;
      } else {
        size = state.length;
      }
      //[from, to)
      byte[] contents = Arrays.copyOfRange(state, startByteIndex, startByteIndex + size);
      startByteIndex = startByteIndex + size;

      head = new RpcTaskHead(
        task.getTaskId(),
        task.getMethodName(),
        parameterTypes,
        null,
        selfPath,
        totalChunkIndex
      );
      return head.setContents(contents);

    } catch (Exception e) {
      LOG.error("generateTaskHead error:{}", e);
      return null;
    }

  }

  RpcTaskChunk generateNextChunk() {
    int size;
    if(state.length - startByteIndex <= baseChunkSize) {
      size = state.length - startByteIndex;
    } else {
      size = baseChunkSize;
    }
    byte[] contents = Arrays.copyOfRange(state, startByteIndex, startByteIndex + size);
    startByteIndex += size;
    return new RpcTaskChunk(head.taskId, ++ lastChunkIndex, contents);
  }

  Procedure<Object> working = new Procedure<Object>() {
    @Override
    public void apply(Object message) {
      if(message instanceof RpcTask) {
        RpcTask task = (RpcTask) message;
        LOG.debug("{} got message: {}", selfName, task);
        task.updateClientPath(selfPath);
        try {
          client = sender();
//          ActorRef self = self();
          RpcTaskHead head = generateTaskHead(task);

          ActorSelection selection = getContext().actorSelection(task.getServicePath());
          selection.tell(head, self());

          getContext().become(waiting);

        } catch (Exception e) {
          LOG.error("working RpcTask error {}", e);
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
      LOG.debug("{} waiting got message: {}", selfName, message);

      if(message instanceof GlobalRpcResult) {
        GlobalRpcResult result = (GlobalRpcResult) message;
        taskResult = result;
        client.tell(result, ActorRef.noSender());
        //only make sure during one duration, operation is unique, no longer preserve result after that,
        //it means if you submit task with same task id after a duration, your task will be processed
        context().setReceiveTimeout(Duration.create(1, TimeUnit.DAYS));
        getContext().become(completing);
      } else if(message instanceof RpcTask) {
//        RpcTask task = (RpcTask) message;
        sender().tell(
          GlobalRpcResult.failure(
            100303L,
            "task has been submitted but not completed, please try again later to get the taskResult."
          ), ActorRef.noSender());

      } else if(message instanceof RpcTaskChunkReply) {
        RpcTaskChunkReply reply = (RpcTaskChunkReply) message;
        int chunkIndex = reply.chunkIndex;
        if(chunkIndex < totalChunkIndex) {
          RpcTaskChunk chunk = generateNextChunk();
          sender().tell(chunk, self());
        }

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


}
