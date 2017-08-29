/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.protocol.RemoteProtocol;

/**
 * Created by zhuyuqing on 2017/8/3.
 */

public class LocateService implements RemoteProtocol {

  public String serviceName;

  public String serviceType;

  public Object resourceId;

  public String requestAddress;

  public GlobalRpcClient.Scale scale;

  public LocateService(String serviceName, String serviceType, Object resourceId, GlobalRpcClient.Scale scale){
    this.serviceName = serviceName;
    this.serviceType = serviceType;
    this.resourceId = resourceId;
    this.scale = scale;
  }

  public LocateService setRequestAddress(String address){
    this.requestAddress = address;
    return this;
  }

  @Override
  public String toString(){
    return "LocateService(" +
      serviceName +
      "," +
      serviceType +
      "," +
//      resourceId +
//      "," +
      scale +
      "," +
      requestAddress +
      ")";
  }

  @Override
  public int hashCode(){

      int result = serviceName.hashCode();
      result = 29 * result + serviceType.hashCode();
      result = 30 * result + resourceId.hashCode();
      result = 31 * result + scale.hashCode();
      return result;

  }

  @Override
  public boolean equals(Object other) {
    if(this == other)                                      //先检查是否其自反性，后比较other是否为空。这样效率高
      return true;
    if(other == null || !(other instanceof LocateService))
      return false;

    final LocateService o = (LocateService) other;

    return serviceName.equals(o.serviceName)
      && serviceType.equals(o.serviceType)
      && resourceId.equals(o.resourceId)
      && scale.equals(o.scale);
  }



}
