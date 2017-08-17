/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class GlobalRpcUtils {

  private static Logger LOG = LoggerFactory.getLogger("org.cmcc.aero.impl.rpc.GlobalRpcUtils");

  public static GlobalRpcResult invoke(GlobalRpcIntf rpcSvc, String methodName, Object[] parameters) {
    try {
      Method method = getValidMethod(rpcSvc, methodName, parameters);
      Object res = method.invoke(rpcSvc, parameters);
      GlobalRpcResult r;
      if(res instanceof java.util.concurrent.Future) {
        try {
          r = GlobalRpcResult.success(((Future) res).get(10, TimeUnit.SECONDS));
        }  catch (Exception e){
          LOG.error("GlobalRpcUtils invoke {} method {} parameters {} future wait error:{}",
            rpcSvc.getClass(),
            methodName,
            parameters,
            e.getMessage());
          r = GlobalRpcResult.failure(100201L, e.getMessage());
        }
      } else {
        r = GlobalRpcResult.success(res);
      }

      return r;

    } catch (Exception e) {
      LOG.error("GlobalRpcUtils invoke {} method {} parameters {} error:{}",
        rpcSvc.getClass(),
        methodName,
        parameters,
        e.getMessage(),e);
      return GlobalRpcResult.failure(100200L, e.getMessage());
    }
  }

  private static Method getValidMethod(GlobalRpcIntf rpcSvc, String methodName, Object[] parameters) throws
          NoSuchMethodException{
    Class<?> clz = rpcSvc.getClass();
    for(Method method : clz.getMethods()){
      if(! method.getName().equals(methodName))
        continue;
      if(method.getParameterCount() != parameters.length)
        continue;
      Class<?>[] parameterTypes =  method.getParameterTypes();
      int i=0;
      for(i=0; i< parameters.length; i++){
        Class<?> paraType = parameterTypes[i];
        if(! (paraType.isAssignableFrom(parameters[i].getClass())))
          break;
      }
      if(i == parameters.length)
        return method;
    }
    String erroMsg = String.format("No method %s found with parameters %s", methodName, parameters.toString());
    throw new NoSuchMethodException(erroMsg);
  }


}
