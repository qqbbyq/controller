package akka.cluster.pubsub

import akka.actor.ActorRef

/**
  * User: zhuyuqing
  * Date: 2017/7/13
  * Time: 15:43
  */
abstract class AbstractDistributedPubSubMediator(settings:DistributedPubSubSettings) extends DistributedPubSubMediator(settings) {

    override def preStart(): Unit = super.preStart()

    override def receive : Receive = extendReceive.orElse(super.receive)

    def extendReceive : Receive

    override def newTopicActor(encTopic: String): ActorRef = super.newTopicActor(encTopic)
}
