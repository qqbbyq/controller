package org.cmcc.aero.impl.rpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.Recover;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.cmcc.aero.impl.rpc.message.*;
import org.cmcc.aero.impl.rpc.server.RpcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;
/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class GlobalRpcClient {

  Logger LOG = LoggerFactory.getLogger(this.getClass());

  public static ActorSystem actorSystem = ActorSystem.create("globalRpcSystem");
  private static ActorRef rpcManager = actorSystem.actorOf(Props.create(RpcManager.class), "rpcManager");

  private GlobalRpcClient(){}

  public static GlobalRpcClient getInstance(){
    return new GlobalRpcClient();
  }

  public void register(GlobalRpcIntf service, String serviceName, String serviceType){
    rpcManager.tell(new RegisterService(service, serviceName, serviceType), ActorRef.noSender());
  }

  // auto invoke service.isResourceLocal()
  public String locate(String serviceName, String serviceType, Object resourceId, Scale scale) {
    try {
      Timeout timeout = new Timeout(Duration.create(5, "seconds"));

      Future<String> future = Patterns.ask(
        rpcManager,
        new LocateService(serviceName, serviceType, resourceId, scale),
        timeout).recoverWith(new Recover<scala.concurrent.Future<String>>(){
          @Override
          public scala.concurrent.Future<String> recover(Throwable failure) throws Throwable {
            return Futures.future(() -> "", actorSystem.dispatcher());
          }
      }, actorSystem.dispatcher());
      return Await.result(future, timeout.duration());
    } catch (Exception e){
      LOG.error("locate await Error:{}", e.getMessage());
      return "";
    }
  }

  public Future<GlobalRpcResult> globalCall(String callId, String servicePath, String methodName, Object... parameters){

    RpcTask task = RpcTask.create(callId, servicePath, methodName, parameters);
    Timeout timeout = new Timeout(Duration.create(10, "seconds"));

    scala.concurrent.Future<GlobalRpcResult> future = Patterns.ask(rpcManager, task, timeout)
      .recoverWith(new Recover<scala.concurrent.Future<GlobalRpcResult>>() {
        @Override
        public scala.concurrent.Future<GlobalRpcResult> recover(Throwable failure) throws Throwable {
          if (failure instanceof TimeoutException) {
            return Futures.future(() -> GlobalRpcResult.failure(100101L, failure.getMessage()), actorSystem.dispatcher());
          } else {
            return Futures.future(() -> GlobalRpcResult.failure(100100L, failure.getMessage()), actorSystem.dispatcher());
          }
        }
      },actorSystem.dispatcher());

    return future;
  }


  public enum Scale {
    LOCAL,
    CLUSTER
  }

  protected static void updateRpcManager(ActorRef manager, ActorSystem system){
    actorSystem = system;
    rpcManager = manager;
  }



}
