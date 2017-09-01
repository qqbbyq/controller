package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.RemoteProtocol;
import org.cmcc.aero.impl.rpc.protocol.TaskProtocol;

/**
 * Created by zhuyuqing on 2017/8/30.
 */

public class RpcTaskChunkReply  extends TaskProtocol implements RemoteProtocol{

  public String taskId;

  public int chunkIndex;

  public RpcTaskChunkReply(String taskId, int chunkIndex) {
    this.taskId = taskId;
    this.chunkIndex = chunkIndex;
  }

  @Override
  public String toString(){
    return "RpcTaskChunkReply(" +
      taskId +
      "," +
      chunkIndex +
      ")";
  }

}
