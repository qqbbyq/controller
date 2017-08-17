/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.RemoteProtocol;

import java.util.Arrays;

/**
 * Created by zhuyuqing on 2017/8/1.
 */

public class RpcTask implements RemoteProtocol {

  private static final long serialVersionUID = 1L;

  private String taskId;

  private String servicePath;

  private String methodName;

  private Object[] parameters;

  private RpcTask(String id,
                  String address,
                  String methodName,
                  Object[] parameters) {
    this.taskId = id;
    this.servicePath = address;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public String getTaskId() {
    return this.taskId;
  }

  public String getMethodName() {
    return this.methodName;
  }

  public Object[] getParameters() {
    return this.parameters;
  }

  public String getServicePath() {
    return this.servicePath;
  }

  public static RpcTask create(String taskId,
                               String path,
                               String methodName,
                               Object[] parameters) {

    return new RpcTask(taskId, path, methodName, parameters);
  }

  @Override
  public String toString(){
    return "RpcTask(" +
      taskId +
      "," +
      servicePath +
      "," +
      methodName +
//      "," +
//      Arrays.toString(parameters) +
      ")";
  }


}
