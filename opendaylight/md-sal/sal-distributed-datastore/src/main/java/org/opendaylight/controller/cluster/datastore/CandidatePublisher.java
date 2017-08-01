package org.opendaylight.controller.cluster.datastore;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.CandidateWrapper;
import akka.cluster.pubsub.DistributedPubSubMediator;
import org.opendaylight.controller.cluster.datastore.messages.CandidatePayload;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: zhuyuqing
 * Date: 2017/7/20
 * Time: 9:56
 */
public class CandidatePublisher extends AbstractActor {

  static ActorRef mediator;

  private static Logger LOG = LoggerFactory.getLogger("org.opendaylight.controller.cluster.datastore.CandidatePublisher");

//  private static CandidatePublisher  instance;

  public static void setMediator(ActorRef mediator){
    CandidatePublisher.mediator = mediator;
    LOG.info("CANTEST: candidate publisher set mediator completed!");
  }

  public static void  publish(long transactionId, DataTreeCandidate candidate){
//    LOG.info("CANTEST: got wrapper type={},path={}!",
//      candidate.getRootNode().getModificationType(),
//      candidate.getRootPath().toString());

    if(mediator == null || mediator.isTerminated()){
      LOG.error("CANTEST: CandidatePublisher mediator has not been initialed or has been terminated!");
      return;
    }
    mediator.tell(new DistributedPubSubMediator.Publish(Shard.CANDIDATE_TOPIC,
      new CandidateWrapper(transactionId, 
        candidate)
        // CandidatePayload.create(candidate))
      ), ActorRef.noSender());
  }

//  private CandidatePublisher() {
//  }

//  public static CandidatePublisher getInstance(){
//    if (instance == null) {
//      synchronized (CandidatePublisher.class) {
//        if (instance == null) {
//          instance = new CandidatePublisher();
//        }
//      }
//    }
//    return instance;
//  }



}
