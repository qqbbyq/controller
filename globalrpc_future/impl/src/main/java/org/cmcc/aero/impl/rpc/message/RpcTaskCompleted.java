package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.Event;

/**
 * Created by zhuyuqing on 2017/8/11.
 */

public class RpcTaskCompleted implements Event {
  public String taskId;

  public RpcTaskCompleted(String taskId){
    this.taskId = taskId;
  }

  @Override
  public String toString(){
    return "RpcTaskCompleted(" + taskId + ")";
  }

  @Override
  public String getTaskId() {
    return this.taskId;
  }
}
