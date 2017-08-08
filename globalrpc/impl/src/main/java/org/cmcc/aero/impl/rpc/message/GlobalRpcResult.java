package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.RemoteProtocol;

/**
 * Created by zhuyuqing on 2017/8/1.
 */

public class GlobalRpcResult implements RemoteProtocol {

  private long errorCode;
  private String errorMessage;
  private Object data;

  public static GlobalRpcResult success(){
    return new GlobalRpcResult(null);
  }

  public static GlobalRpcResult success(Object data){
    return new GlobalRpcResult(data);
  }

  public static GlobalRpcResult failure(long errorCode, String errorMessage){
    return new GlobalRpcResult(errorCode, errorMessage);
  }

  private GlobalRpcResult(Object data){
    this(0L, "ok", data);
  }

  private GlobalRpcResult(long errorCode, String errorMessage){
    this(errorCode, errorMessage, null);
  }

  private GlobalRpcResult(long errorCode, String errorMessage, Object data) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.data =  data;
  }

  public long getErrorCode(){
    return this.errorCode;
  }

  public String getErrorMessage(){
    return this.errorMessage;
  }

  public Object getData() {
    return this.data;
  }

  @Override
  public String toString(){
    return "GlobalRpcResult(" + errorCode + "," + errorMessage + "," + data + ")";
  }
}
