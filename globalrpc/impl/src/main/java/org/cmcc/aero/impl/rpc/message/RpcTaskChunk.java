package org.cmcc.aero.impl.rpc.message;

import org.cmcc.aero.impl.rpc.protocol.TaskProtocol;

import java.io.Serializable;

/**
 * Created by zhuyuqing on 2017/8/30.
 */

public class RpcTaskChunk extends TaskProtocol implements Serializable {

  public String taskId;

  public int chunkIndex;

  public byte[] contents;

  public RpcTaskChunk(String taskId, int chunkIndex, byte[] contents) {
    this.taskId = taskId;
    this.chunkIndex = chunkIndex;
    this.contents = contents;
  }

  @Override
  public String toString(){
    return "RpcTaskChunk(" +
      taskId +
      "," +
      chunkIndex +
      ")";
  }
}
