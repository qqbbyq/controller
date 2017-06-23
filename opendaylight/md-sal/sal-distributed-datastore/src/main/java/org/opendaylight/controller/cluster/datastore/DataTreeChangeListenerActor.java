/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.datastore;

import akka.actor.Props;
import com.google.common.base.Preconditions;
import org.opendaylight.controller.cluster.common.actor.AbstractUntypedActor;
import org.opendaylight.controller.cluster.datastore.messages.DataTreeChanged;
import org.opendaylight.controller.cluster.datastore.messages.DataTreeChangedReply;
import org.opendaylight.controller.cluster.datastore.messages.EnableNotification;
import org.opendaylight.controller.md.sal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxy actor which acts as a facade to the user-provided listener. Responsible for decapsulating
 * DataTreeChanged messages and dispatching their context to the user.
 */
//DataTreeChangeListener作为向用户传递Changed消息的对外窗口存在,
// 谁往这里发送了DataTreeChanged的消息？？？猜测是ForwardingDataTreeChangeListener??TODO
// 回复了一个DataTreeChangedReply
final class DataTreeChangeListenerActor extends AbstractUntypedActor {
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeChangeListenerActor.class);
    private final DOMDataTreeChangeListener listener;
    private final YangInstanceIdentifier registeredPath;
    private boolean notificationsEnabled = false;

    //按理说这里的listener就是用户的listener了吧,Proxy里传入的listener TODO: not sure
    private DataTreeChangeListenerActor(final DOMDataTreeChangeListener listener,
            final YangInstanceIdentifier registeredPath) {
        this.listener = Preconditions.checkNotNull(listener);
        this.registeredPath = Preconditions.checkNotNull(registeredPath);
    }

    @Override //只有enable了notification之后才会处理dataChanged messages
    protected void handleReceive(final Object message) {
        if (message instanceof DataTreeChanged) {
            dataChanged((DataTreeChanged)message);
        } else if (message instanceof EnableNotification) {
            enableNotification((EnableNotification) message);
        } else {
            unknownMessage(message);
        }
    }

    private void dataChanged(final DataTreeChanged message) {
        // Do nothing if notifications are not enabled
        if (!notificationsEnabled) {
            LOG.debug("Notifications not enabled for listener {} - dropping change notification", listener);
            return;
        }

        LOG.debug("Sending change notification {} to listener {}", message.getChanges(), listener);

        try {//调用用户端listener接口的onDataChangedxxxx
            this.listener.onDataTreeChanged(message.getChanges());
        } catch (Exception e) {
            LOG.error("Error notifying listener {}", this.listener, e);
        }

        // TODO: do we really need this?
        // It seems the sender is never null but it doesn't hurt to check. If the caller passes in
        // a null sender (ActorRef.noSender()), akka translates that to the deadLetters actor.
        if (getSender() != null && !getContext().system().deadLetters().equals(getSender())) {
            getSender().tell(DataTreeChangedReply.getInstance(), getSelf());
        }
    }

    private void enableNotification(final EnableNotification message) {
        notificationsEnabled = message.isEnabled();
        LOG.debug("{} notifications for listener {}", (notificationsEnabled ? "Enabled" : "Disabled"),
                listener);
    }

    public static Props props(final DOMDataTreeChangeListener listener, final YangInstanceIdentifier registeredPath) {
        return Props.create(DataTreeChangeListenerActor.class, listener, registeredPath);
    }
}
