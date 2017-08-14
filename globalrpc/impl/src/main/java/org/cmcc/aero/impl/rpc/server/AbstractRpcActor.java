/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyuqing on 2017/8/7.
 */

public abstract class AbstractRpcActor extends UntypedActor {


  public abstract void extendReceive(Object message);

  private Logger LOG = LoggerFactory.getLogger(this.getClass());


  @Override
  public void preStart() throws Exception {
    super.preStart();
    Cluster.get(getContext().system()).subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
      MemberEvent.class, UnreachableMember.class);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    if (!baseReceive(message)) {
      extendReceive(message);
    }
  }

  private boolean baseReceive(Object message) {
    if (message instanceof MemberUp) {
      LOG.info("Member is Up: {}", ((MemberUp) message).member());
      return true;
    } else if (message instanceof UnreachableMember) {
      LOG.warn("Member detected as unreachable: {}", ((UnreachableMember) message).member());
      return true;
    } else if (message instanceof MemberRemoved) {
      LOG.warn("Member is Removed: {}", ((MemberRemoved) message).member());
      return true;
    } else if (message instanceof MemberEvent) {
      //ignore
      return true;
    }
    return false;
  }
}
