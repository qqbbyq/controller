package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.cmcc.aero.impl.rpc.protocol.LocalProtocol;

/**
 * Created by zhuyuqing on 2017/8/3.
 */

public class RegisterService implements LocalProtocol {

  public String serviceName;

  public String serviceType;

  public GlobalRpcIntf service;

  public RegisterService(GlobalRpcIntf service, String serviceName, String serviceType){
    this.serviceName = serviceName;
    this.serviceType = serviceType;
    this.service = service;
  }

  @Override
  public String toString(){
    return "RegisterService(" +
      serviceName +
      "," +
      serviceType +
      "," +
      service +
      ")";
  }

}
