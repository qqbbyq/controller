package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.RemoteProtocol;
import org.cmcc.aero.impl.rpc.protocol.TaskProtocol;

import java.util.List;

/**
 * Created by zhuyuqing on 2017/8/30.
 */

public class RpcTaskHead  extends TaskProtocol implements RemoteProtocol   {
  public static final long serialVersionUID = 1L;

  public String taskId;

  public String methodName;

  public List<Class<?>> parameterTypes;

  public byte[] contents;

  public String clientPath;

  public int totalChunkIndex;

  public RpcTaskHead(
    String taskId,
    String methodName,
    List<Class<?>> types,
    byte[] contents,
    String clientPath,
    int totalChunkIndex
  ) {
    this.taskId = taskId;
    this.methodName = methodName;
    this.parameterTypes = types;
    this.contents = contents;
    this.clientPath = clientPath;
    this.totalChunkIndex = totalChunkIndex;
  }

  public RpcTaskHead setContents(byte[] contents) {
    this.contents = contents;
    return this;
  }

  @Override
  public String toString(){
    return "RpcTaskHead(" +
      taskId +
      "," +
      methodName +
      "," +
      clientPath +
      "," +
      totalChunkIndex +
      ")";
  }
}
