/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc.server;

import akka.actor.AbstractActor;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyuqing on 2017/8/7.
 */

public class AbstractRpcActor extends AbstractActor {

  public Receive extendReceive;

  private Logger LOG = LoggerFactory.getLogger(this.getClass());



  Receive baseReceive() {
    return receiveBuilder()
      .match(MemberUp.class, mUp -> {
        LOG.info("Member is Up: {}", mUp.member());
      })
      .match(UnreachableMember.class, mUnreachable -> {
        LOG.info("Member detected as unreachable: {}", mUnreachable.member());
      })
      .match(MemberRemoved.class, mRemoved -> {
        LOG.info("Member is Removed: {}", mRemoved.member());
      })
      .match(MemberEvent.class, message -> {
        //ignore
      })
      .build();
  }

  @Override
  public Receive createReceive() {
    return baseReceive();
  }
}
