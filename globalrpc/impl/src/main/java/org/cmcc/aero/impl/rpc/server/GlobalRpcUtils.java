/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.controller.cluster.datastore.node.utils.serialization.NormalizedNodeSerializer;
import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class GlobalRpcUtils {

  private static Logger LOG = LoggerFactory.getLogger("org.cmcc.aero.impl.rpc.GlobalRpcUtils");

  public static class MethodKey {
    public Class<?> service;
    public String methodName;
    public Class<?>[] parameterTypes;

    public MethodKey(Class<?> service, String methodName, Class<?>[] parameters) {
      this.service = service;
      this.methodName = methodName;
      this.parameterTypes = parameters;
    }

    @Override
    public int hashCode(){

      int result = service.hashCode();
      result = 29 * result + methodName.hashCode();
      for(Class<?> type: parameterTypes) {
        result += type.hashCode();
      }
      return result;

    }

    @Override
    public boolean equals(Object other) {
      if(this == other)                                      //先检查是否其自反性，后比较other是否为空。这样效率高
        return true;
      if(other == null || !(other instanceof MethodKey))
        return false;

      final MethodKey o = (MethodKey) other;
      if(service.equals(o.service)
        && methodName.equals(o.methodName)
        && parameterTypes.length == o.parameterTypes.length) {

        for(int i = 0; i < parameterTypes.length; ++i) {
          if(!parameterTypes[i].equals(o.parameterTypes[i])){
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }

  //TODO method cache
  private static LoadingCache<MethodKey, Method> methodCache = CacheBuilder.newBuilder()//
    .refreshAfterWrite(600, TimeUnit.SECONDS)// 给定时间内没有被读/写访问，则回收。
//    .expireAfterWrite(30, TimeUnit.SECONDS)//给定时间内没有写访问，则回收。
    // .expireAfterAccess(3, TimeUnit.SECONDS)// 缓存过期时间为3秒
    .maximumSize(100).// 设置缓存个数
    build(new CacheLoader<MethodKey, Method>() {

    public Method load(MethodKey key) throws ExecutionException, NoSuchMethodException {
      LOG.info(key + " load in cache");
      return getValidMethod(key.service, key.methodName, key.parameterTypes);
    }
  });

  public static GlobalRpcResult invoke(GlobalRpcIntf rpcSvc, String methodName, Object[] parameters) {
    try {
      parameters = fromSerialized(parameters);
      LOG.debug("invoke service: {}", rpcSvc.getClass().toString());
      LOG.debug("invoke method: {}", methodName);

      Class<?>[] parameterTypes = new Class[parameters.length];
      for(int i = 0 ; i < parameters.length; ++i) {
//        LOG.info("invoke prameter: {}", parameterTypes[i]);
        parameterTypes[i] = parameters[i].getClass();
        LOG.debug("invoke prameterTypes: {}", parameterTypes[i]);
      }
      Method method = methodCache.get(new MethodKey(rpcSvc.getClass(), methodName, parameterTypes));
      Object res = method.invoke(rpcSvc, parameters);
      GlobalRpcResult r;
      if(res instanceof java.util.concurrent.Future) {
        try {
          r = GlobalRpcResult.success(((Future) res).get(10, TimeUnit.SECONDS));
        }  catch (Exception e){
          LOG.error("GlobalRpcUtils invoke {} method {} parameters {} java future wait error:{}",
            rpcSvc.getClass(),
            methodName,
            parameterTypes,
            e.getMessage());
          r = GlobalRpcResult.failure(100201L, e.getMessage());
        }
      }  else {
        r = GlobalRpcResult.success(res);
      }
      LOG.info("invoke service={}, method={}, parameter.length={}, result={}", rpcSvc, methodName, parameters.length, r);
      return r;

    } catch (Exception e) {
      LOG.error("GlobalRpcUtils invoke {} method {} error:{}",
        rpcSvc.getClass(),
        methodName,
        e);
      return GlobalRpcResult.failure(100200L, e.getMessage());
    }
  }

  private static Method getValidMethod(Class<?> rpcSvc, String methodName, Class<?>[] parameters) throws
          NoSuchMethodException{
//    Class<?> clz = rpcSvc.getClass();
    for(Method method : rpcSvc.getMethods()){
      if(! method.getName().equals(methodName))
        continue;
      if(method.getParameterCount() != parameters.length)
        continue;
      Class<?>[] parameterTypes =  method.getParameterTypes();
      int i=0;
      for(i=0; i < parameters.length; i++){
        Class<?> paraType = parameterTypes[i];
        if(! (paraType.isAssignableFrom(parameters[i])))
          break;
      }
      if(i == parameters.length)
        return method;
    }
    String erroMsg = String.format("No method %s found with parameters %s", methodName, parameters);
    throw new NoSuchMethodException(erroMsg);
  }


  public static Object[] toSerialize(Object[] parameters) {
    List<Object> serialParas = new ArrayList<>();
    for(Object parameter: parameters) {
      if(parameter instanceof NormalizedNode) {
        serialParas.add(NormalizedNodeSerializer.serialize((NormalizedNode) parameter));
      } else {
        serialParas.add(parameter);
      }
    }
    return serialParas.toArray();
  }

  private static Object[] fromSerialized(Object[] parameters) {
    List<Object> paras = new ArrayList<>();
    for(Object parameter: parameters)
    if(parameter instanceof NormalizedNodeMessages.Node) {
      paras.add(NormalizedNodeSerializer.deSerialize((NormalizedNodeMessages.Node) parameter));
    } else {
      paras.add(parameter);
    }
    return paras.toArray();
  }

}
