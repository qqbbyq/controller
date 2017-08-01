package org.opendaylight.controller.cluster.datastore.messages;

import akka.serialization.JSerializer;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.opendaylight.controller.cluster.datastore.persisted.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

import java.io.IOException;

/**
 * User: zhuyuqing
 * Date: 2017/7/24
 * Time: 15:32
 */
public class DataTreeCandidateSerializer extends JSerializer{

  @Override
  public int identifier() {
    return 97439438;
  }

  @Override
  public boolean includeManifest() {
    return false;
  }

  @Override
  public byte[] toBinary(final Object obj) {
    Preconditions.checkArgument(obj instanceof DataTreeCandidate,
      "Unsupported object type %s", obj.getClass());
    final DataTreeCandidate candidate = (DataTreeCandidate) obj;

    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    try {
      DataTreeCandidateInputOutput.writeDataTreeCandidate(out, candidate);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Failed to serialize candidate %s", candidate), e);
    }

    return out.toByteArray();
  }

  @Override
  public Object fromBinaryJava(final byte[] bytes, final Class<?> clazz) {
    DataTreeCandidate candidate;
    try {
      candidate = DataTreeCandidateInputOutput.readDataTreeCandidate(ByteStreams.newDataInput(bytes));
    } catch (Exception e){
      throw new IllegalArgumentException(String.format("Failed to deserialize candidate length=%s", bytes.length), e);
    }
    return candidate;

  }

}
