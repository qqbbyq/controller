package org.opendaylight.controller.cluster.datastore.messages;

import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

import java.io.Serializable;
import java.util.List;

/**
 * User: zhuyuqing
 * Date: 2017/7/10
 * Time: 14:46
 */
public final class CandidateSubmit implements Serializable {

  private final long transactionId;
  private final List<DataTreeCandidate> candidates;

  CandidateSubmit(long transactionId,  List<DataTreeCandidate> candidate){
    this.transactionId = transactionId;
    this.candidates = candidate;
  }

  public long getTransactionId(){
    return transactionId;
  }

  public List<DataTreeCandidate> getCandidates(){
    return candidates;
  }

  public static CandidateSubmit create(long transactionId, List<DataTreeCandidate> candidates){
    return new CandidateSubmit(transactionId, candidates);
  }

  public long size() {
    return candidates.size();
  }

}

