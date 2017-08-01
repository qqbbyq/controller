package akka.cluster.pubsub

import akka.actor.{ActorRef, ActorSystem, Deploy, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props, Stash, Terminated}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSubMediator.Internal._
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.dispatch.Dispatchers
import akka.routing.RoutingLogic
import org.opendaylight.controller.cluster.datastore.CandidateSubmit
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.Set
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * User: zhuyuqing
  * Date: 2017/7/13
  * Time: 17:01
  */
class ModifiedDistributedPubSubMediator(settings: DistributedPubSubSettings) extends AbstractDistributedPubSubMediator(settings){

  val LOG: Logger = LoggerFactory.getLogger(this.getClass)
  import cluster.selfAddress

  import scala.concurrent.duration._
  import org.opendaylight.controller.cluster.datastore.messages._


  override def preStart(): Unit = {
    super.preStart()
    LOG.info(s"${self.path.toString} starts.")
  }

  def extendReceive : Receive = {
    case msg @ Subscribe(topic, _, _) ⇒
      // each topic is managed by a child actor with the same name as the topic

      val encTopic = encName(topic)

      bufferOr(mkKey(self.path / encTopic), msg, sender()) {
        context.child(encTopic) match {
          case Some(t) ⇒ t forward msg
          case None    ⇒ newTopicActor(encTopic) forward msg
        }
      }

    case Terminated(a) ⇒
      val key = mkKey(a)
      registry(selfAddress).content.get(key) match {
        case Some(ValueHolder(_, Some(`a`))) ⇒
          // remove
          put(key, None)
        case _ ⇒
      }
      recreateAndForwardMessagesIfNeeded(key, newTopicActor(a.path.name))

  }

  import settings._
  override def newTopicActor(encTopic: String): ActorRef = {
    val t = context.actorOf(Props(new ModifiedTopic(removedTimeToLive, routingLogic)), name = encTopic)
    registerTopic(t)
    t
  }


  case class BroadcastCompleted(transactionId: Long, success: Set[String], fail: Set[String])
  case object Tick

  /*object ModifiedTopic{

    def props(emptyTimeToLive: FiniteDuration, routingLogic: RoutingLogic): Props ={
      Props.create(classOf[ModifiedTopic], emptyTimeToLive, routingLogic)
    }
  }*/

  class ModifiedTopic(val emptyTimeToLive: FiniteDuration, routingLogic: RoutingLogic) extends TopicLike with PerGroupingBuffer
                                                                                               with Stash {

    //    val LOG: Logger = LoggerFactory.getLogger(this.getClass)

    val candidateCache = new mutable.Queue[CandidateWrapper]()
    val indexCache = new mutable.HashMap[String, Long]()

    override def preStart(): Unit = {
      super.preStart()
      LOG.info(s"${self.path.toString} starts.")
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(1 second, 1 second, self, Tick)






    def waitComplete: Receive = {
      case msg@BroadcastCompleted(transactionId, success, fail) =>
        LOG.debug(s"${self.path.address} waitComplete got message:$msg")
        val index = candidateCache.indexWhere(_.transactionId == transactionId)
        candidateCache.drop(index + 1)

        success.foreach(indexCache.put(_, transactionId))

        if(fail.nonEmpty) LOG.error(s"${self.path.address}  waitComplete failed lists: {}.", fail.mkString(","))

        context.become(receive)
        unstashAll()

      case msg =>
        stash()
    }

    import akka.pattern.ask

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._

    def extendReceive: Receive = {
      case wrapper@CandidateWrapper(transactionId, candidate) =>
        LOG.debug(s"${self.path.address} extendReceive got message:$wrapper")

        candidateCache.enqueue(wrapper)

        import scala.collection.JavaConversions._

        Future.sequence(subscribers.map{s =>
          if(indexCache.get(mkKey(s)).isEmpty) indexCache.put(mkKey(s), 0l)
          val candidates = candidateCache.slice(
            candidateCache.indexWhere(_.transactionId == indexCache.getOrElse(mkKey(s), 0)) + 1,
            candidateCache.size
          ).toList
          LOG.info(s"send to ${s.path},size=${candidates.size}")
          (s ? CandidateSubmit.create(transactionId, candidates.map(_.candidate)))(1 second).map{_ =>
            (true, mkKey(s))
          }.recover{
            case e: Exception =>
              LOG.error(s"${self.path.address} " + e.getMessage)
              e.printStackTrace()
              (false, mkKey(s))
          }
        }).map{ seq =>
          val (succ, fail) = seq.partition(_._1)
          self ! BroadcastCompleted(transactionId, succ.map(_._2), fail.map(_._2))
        }.onComplete{
          case Success(_) =>
          case Failure(e) =>
            LOG.error(s"${self.path.address} onComplete " + e.getMessage)
            e.printStackTrace()
        }
        context.become(waitComplete)

      case Tick =>
        if(indexCache.nonEmpty){
          val targetId = indexCache.values.min
          LOG.debug(s"${self.path.address} got message: Tick, targetId=$targetId")
          val index = candidateCache.indexWhere(_.transactionId == targetId)
          candidateCache.drop(index + 1)
        }

    }

    def business = {
      case msg @ Subscribe(_, Some(group), _) ⇒
        val encGroup = encName(group)
        bufferOr(mkKey(self.path / encGroup), msg, sender()) {
          context.child(encGroup) match {
            case Some(g) ⇒ g forward msg
            case None    ⇒ newGroupActor(encGroup) forward msg
          }
        }
        pruneDeadline = None
      case msg @ Unsubscribe(_, Some(group), _) ⇒
        val encGroup = encName(group)
        bufferOr(mkKey(self.path / encGroup), msg, sender()) {
          context.child(encGroup) match {
            case Some(g) ⇒ g forward msg
            case None    ⇒ // no such group here
          }
        }
      case msg: Subscribed ⇒
        context.parent forward msg
      case msg: Unsubscribed ⇒
        context.parent forward msg
      case NoMoreSubscribers ⇒
        val key = mkKey(sender())
        initializeGrouping(key)
        sender() ! TerminateRequest
      case NewSubscriberArrived ⇒
        val key = mkKey(sender())
        forwardMessages(key, sender())
      case Terminated(ref) ⇒
        val key = mkKey(ref)
        recreateAndForwardMessagesIfNeeded(key, newGroupActor(ref.path.name))
    }

    def newGroupActor(encGroup: String): ActorRef = {
      val g = context.actorOf(Props(classOf[Group], emptyTimeToLive, routingLogic), name = encGroup)
      context watch g
      context.parent ! RegisterTopic(g)
      g
    }

    override def receive: PartialFunction[Any, Unit] =
      extendReceive.orElse(business.orElse[Any, Unit](defaultReceive))
  }

}

object ModifiedDistributedPubSubMediator{

  def props(settings: DistributedPubSubSettings): Props =
    Props.create(classOf[ModifiedDistributedPubSubMediator], settings).withDeploy(Deploy.local)

}

class ModifiedDistributedPubSub(system: ExtendedActorSystem) extends Extension {

  private val settings = DistributedPubSubSettings(system)

  def isTerminated: Boolean =
    Cluster(system).isTerminated || !settings.role.forall(Cluster(system).selfRoles.contains)

  val mediator: ActorRef = {
    if (isTerminated)
      system.deadLetters
    else {
      val name = system.settings.config.getString("akka.cluster.pub-sub.name")
      val dispatcher = system.settings.config.getString("akka.cluster.pub-sub.use-dispatcher") match {
        case "" ⇒ Dispatchers.DefaultDispatcherId
        case id ⇒ id
      }
      system.systemActorOf(ModifiedDistributedPubSubMediator.props(settings).withDispatcher(dispatcher), name)
    }
  }
}

object ModifiedDistributedPubSub extends ExtensionId[ModifiedDistributedPubSub] with ExtensionIdProvider {
  override def get(system: ActorSystem): ModifiedDistributedPubSub = super.get(system)

  override def lookup = ModifiedDistributedPubSub

  override def createExtension(system: ExtendedActorSystem): ModifiedDistributedPubSub =
    new ModifiedDistributedPubSub(system)
}

case class CandidateWrapper(transactionId: Long, candidate: DataTreeCandidate) extends scala.Serializable

