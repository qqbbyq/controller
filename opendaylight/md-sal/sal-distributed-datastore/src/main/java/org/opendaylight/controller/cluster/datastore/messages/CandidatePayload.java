package org.opendaylight.controller.cluster.datastore.messages;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.opendaylight.controller.cluster.datastore.persisted.DataTreeCandidateInputOutput;
import org.opendaylight.controller.cluster.raft.protobuff.client.messages.Payload;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public final class CandidatePayload extends Payload implements Externalizable {
  private static final long serialVersionUID = 1L;

  private transient byte[] serialized;

  public CandidatePayload() {
    // Required by Externalizable
  }

  private CandidatePayload(final byte[] serialized) {
    this.serialized = Preconditions.checkNotNull(serialized);
  }

  public static CandidatePayload create(final DataTreeCandidate candidate) {
    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
    try {
      DataTreeCandidateInputOutput.writeDataTreeCandidate(out, candidate);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Failed to serialize candidate %s", candidate), e);
    }

    return new CandidatePayload(out.toByteArray());
  }

  public DataTreeCandidate getCandidate() throws IOException {
    return DataTreeCandidateInputOutput.readDataTreeCandidate(ByteStreams.newDataInput(serialized));
  }

  @Override
  public int size() {
    return serialized.length;
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeByte((byte)serialVersionUID);
    out.writeInt(serialized.length);
    out.write(serialized);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final long version = in.readByte();
    Preconditions.checkArgument(version == serialVersionUID, "Unsupported serialization version %s", version);

    final int length = in.readInt();
    serialized = new byte[length];
    in.readFully(serialized);
  }
}

