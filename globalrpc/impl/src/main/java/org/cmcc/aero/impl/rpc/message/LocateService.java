package org.cmcc.aero.impl.rpc.message;

import akka.actor.Address;
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
      resourceId +
      "," +
      scale +
      "," +
      requestAddress +
      ")";
  }



}
