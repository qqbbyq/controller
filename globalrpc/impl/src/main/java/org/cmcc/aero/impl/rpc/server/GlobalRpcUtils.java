package org.cmcc.aero.impl.rpc.server;

import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class GlobalRpcUtils {

  private static Logger LOG = LoggerFactory.getLogger("org.cmcc.aero.impl.rpc.GlobalRpcUtils");

  public static GlobalRpcResult invoke(GlobalRpcIntf rpcSvc, String methodName, Object[] parameters) {
    try {
      Class<?> clz = rpcSvc.getClass();
      List<Class<?>> types = new ArrayList<>();
      Class<?>[] parameterTypes = new Class<?>[]{};

      if (parameters != null) {
        for (Object para : parameters) {
          types.add(para.getClass());
        }
      }

      Method method = clz.getMethod(methodName, types.toArray(parameterTypes));
      return GlobalRpcResult.success(method.invoke(rpcSvc, parameters));
    } catch (Exception e) {
      LOG.error("GlobalRpcUtils invoke {} method {} parameters {} error:{}",
        rpcSvc.getClass(),
        methodName,
        parameters,
        e.getMessage());
      return GlobalRpcResult.failure(100200L, e.getMessage());
    }
  }


}
