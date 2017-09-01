/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.rpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.osgi.BundleDelegatingClassLoader;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.Config;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.message.LocateService;
import org.cmcc.aero.impl.rpc.message.RegisterService;
import org.cmcc.aero.impl.rpc.message.RpcTask;
import org.cmcc.aero.impl.rpc.server.GlobalRpcUtils;
import org.cmcc.aero.impl.rpc.server.RpcManager;
import org.cmcc.aero.impl.utils.AkkaConfig;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class GlobalRpcClient {

  static Logger LOG = LoggerFactory.getLogger("GlobalRpcClient");

  private static ActorSystem actorSystem = null;
  private static ActorRef rpcManager = null;

  private static Timeout timeout = new Timeout(Duration.create(5, "seconds"));

  private static LoadingCache<LocateService, CompletableFuture<String>> locateCache = CacheBuilder.newBuilder()//
//    .refreshAfterWrite(30, TimeUnit.SECONDS)// 给定时间内没有被读/写访问，则回收。
     .expireAfterWrite(600, TimeUnit.SECONDS)//给定时间内没有写访问，则回收。
    // .expireAfterAccess(3, TimeUnit.SECONDS)// 缓存过期时间为3秒
    .maximumSize(100).// 设置缓存个数
    build(new CacheLoader<LocateService, CompletableFuture<String>>() {

      public CompletableFuture<String> load(LocateService key) throws ExecutionException {
        LOG.info(key + " load in cache");
//        LOG.info("already cache:{}", locateCache);
        CompletableFuture<String> future = PatternsCS.ask(
          rpcManager,
          new LocateService(key.serviceName, key.serviceType, key.resourceId, key.scale),
          timeout
           ).toCompletableFuture().handle((result, throwable) -> {
          if (throwable == null)
            return (String) result;
          else {
            LOG.error("locate await error: {}", throwable);
            return "";
          }
        });
        return future;
      }
  });

  public GlobalRpcClient(final BundleContext bundleContext) {

    Bundle bundle = bundleContext.getBundle();
    BundleDelegatingClassLoader classLoader = new BundleDelegatingClassLoader(
      bundle, Thread.currentThread().getContextClassLoader());

    Config config = AkkaConfig.get("application.conf");
    actorSystem = ActorSystem.create("globalRpcSystem", config, classLoader);
    rpcManager = actorSystem.actorOf(Props.create(RpcManager.class), "rpcManager");

  }

  @VisibleForTesting
  private GlobalRpcClient() {
    if(actorSystem == null || rpcManager == null) {
      actorSystem = ActorSystem.create("globalRpcSystem");
      rpcManager = actorSystem.actorOf(Props.create(RpcManager.class), "rpcManager");
    }
  }

  @VisibleForTesting
  public static GlobalRpcClient getTmpInstance() {
    return new GlobalRpcClient();
  }

  public void register(GlobalRpcIntf service, String serviceName, String serviceType){
    rpcManager.tell(new RegisterService(service, serviceName, serviceType), ActorRef.noSender());
  }

  public java.util.concurrent.Future<String> locate(String serviceName, String serviceType, Object resourceId, Scale scale) {
    try {
      return locateCache.get(new LocateService(serviceName, serviceType, resourceId, scale));
    } catch (Exception e) {
      LOG.error("locate service error: {}", e);
      return CompletableFuture.supplyAsync(() -> "");
    }
  }

  public java.util.concurrent.Future<GlobalRpcResult> globalCall(String callId, String servicePath, String methodName, Object... parameters){

    if(servicePath.isEmpty())
      return CompletableFuture.supplyAsync(() -> GlobalRpcResult.failure(100104L, "empty service path"));

    RpcTask task = RpcTask.create(callId, servicePath, methodName, GlobalRpcUtils.toSerialize(parameters));
    LOG.info("globalcall method={}, callId={}, path={}, parameter.length={}", methodName, callId, servicePath, parameters.length);
    Timeout timeout = new Timeout(Duration.create(120, "seconds"));

    CompletableFuture<GlobalRpcResult> future = PatternsCS.ask(rpcManager, task, timeout)
      .toCompletableFuture().handle( (result, throwable) -> {
        if(throwable == null)
          return (GlobalRpcResult) result;
        else {
          LOG.error("globalcall recover error: {}", throwable.getMessage());
          throwable.printStackTrace();
          return GlobalRpcResult.failure(100100L, throwable.getMessage());
        }
    });
    return future;
  }


  public enum Scale {
    LOCAL,
    CLUSTER
  }

  @VisibleForTesting
  protected static void updateRpcManager(ActorRef manager, ActorSystem system){
    actorSystem = system;
    rpcManager = manager;
  }



}
