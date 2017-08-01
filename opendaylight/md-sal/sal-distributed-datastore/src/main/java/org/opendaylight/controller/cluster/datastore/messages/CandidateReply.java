package org.opendaylight.controller.cluster.datastore.messages;


import java.io.Serializable;

/**
 * User: zhuyuqing
 * Date: 2017/7/10
 * Time: 14:47
 */
public final class CandidateReply implements Serializable {

  private long transactionId;

  CandidateReply(long transactionId){
    this.transactionId = transactionId;
  }

  public long getTransactionId(){
    return transactionId;
  }

  public static CandidateReply create(long transactionId){
    return new CandidateReply(transactionId);
  }
}
