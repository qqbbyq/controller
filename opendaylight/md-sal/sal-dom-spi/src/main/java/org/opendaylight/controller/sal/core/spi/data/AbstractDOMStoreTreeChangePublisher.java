/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sal.core.spi.data;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.controller.md.sal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.controller.md.sal.dom.spi.AbstractRegistrationTree;
import org.opendaylight.controller.md.sal.dom.spi.RegistrationTreeNode;
import org.opendaylight.controller.md.sal.dom.spi.RegistrationTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DOMStoreTreeChangePublisher} implementations.
 */
public abstract class AbstractDOMStoreTreeChangePublisher extends AbstractRegistrationTree<AbstractDOMDataTreeChangeListenerRegistration<?>> implements DOMStoreTreeChangePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDOMStoreTreeChangePublisher.class);

    /**
     * Callback for subclass to notify specified registrations of a candidate at a specified path. This method is guaranteed
     * to be only called from within {@link #processCandidateTree(DataTreeCandidate)}.
     *
     * @param registrations Registrations which are affected by the candidate node
     * @param path Path of changed candidate node. Guaranteed to match the path specified by the registration
     * @param node Candidate node
     */
    protected abstract void notifyListeners(@Nonnull Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> registrations, @Nonnull YangInstanceIdentifier path, @Nonnull DataTreeCandidateNode node);

    /**
     * Callback notifying the subclass that the specified registration is being closed and it's user no longer
     * wishes to receive notifications. This notification is invoked while the {@link org.opendaylight.yangtools.concepts.ListenerRegistration#close()}
     * method is executing. Subclasses can use this callback to properly remove any delayed notifications pending
     * towards the registration.
     *
     * @param registration Registration which is being closed
     */
    protected abstract void registrationRemoved(@Nonnull AbstractDOMDataTreeChangeListenerRegistration<?> registration);

    /**
     * Process a candidate tree with respect to registered listeners.
     *
     * @param candidate candidate tree which needs to be processed
     *///处理candidate
    protected final void processCandidateTree(@Nonnull final DataTreeCandidate candidate) {
        final DataTreeCandidateNode node = candidate.getRootNode();
        if (node.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", candidate);
            return;
        }

        try (final RegistrationTreeSnapshot<AbstractDOMDataTreeChangeListenerRegistration<?>> snapshot = takeSnapshot()) {
            final List<PathArgument> toLookup = ImmutableList.copyOf(candidate.getRootPath().getPathArguments());
            //这里的snapshot.getRootNode是注册树快照的根节点
            lookupAndNotify(toLookup, 0, snapshot.getRootNode(), candidate);
        }
    }

    @Override
    public final <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L> registerTreeChangeListener(final YangInstanceIdentifier treeId, final L listener) {
        // Take the write lock
        takeLock();//加锁
        try {
            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node = findNodeFor(treeId.getPathArguments());
            //继承自AbstractRegistrationTree,找到相应registration树上的节点
            final AbstractDOMDataTreeChangeListenerRegistration<L> reg = new AbstractDOMDataTreeChangeListenerRegistration<L>(listener) {
                @Override
                protected void removeRegistration() {
                    AbstractDOMStoreTreeChangePublisher.this.removeRegistration(node, this);
                    registrationRemoved(this);
                }
            };

            addRegistration(node, reg); //继承自AbstractRegistrationTree，在RegistrationTree节点上注册包含listener的注册信息
            return reg; //返回注册信息
        } finally {
            // Always release the lock
            releaseLock();//释放锁
        }
    }


    private void lookupAndNotify(
      final List<PathArgument> args,//candidate的rootPath
      final int offset,//游标初始为0
      final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> node,//注册树
      final DataTreeCandidate candidate
    ) {
        if (args.size() != offset) {
            //分别取出offset层的节点的pathArgument
            final PathArgument arg = args.get(offset);
            //从当前节点寻找path匹配的child节点
            final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> exactChild = node.getExactChild(arg);
            //若child不为空,继续找下一层
            if (exactChild != null) {
                lookupAndNotify(args, offset + 1, exactChild, candidate);
            }

            //寻找path不完全匹配的child节点，继续向下寻找 TODO：？？？？why
            for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> c : node.getInexactChildren(arg)) {
                lookupAndNotify(args, offset + 1, c, candidate);
            }
        } else {
            //wRONG!!!直到找到args.size的最后一层，进行通知，走到所有的最底层的叶子结点！!
            //直到path走完，或者注册树走到了头。
            notifyNode(candidate.getRootPath(), node, candidate.getRootNode());
        }
    }

    private void notifyNode(
      final YangInstanceIdentifier path,
      final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regNode,
      final DataTreeCandidateNode candNode) {
        if (candNode.getModificationType() == ModificationType.UNMODIFIED) {
            LOG.debug("Skipping unmodified candidate {}", path);
            return;
        }
        //找到publicRegistrations
        final Collection<AbstractDOMDataTreeChangeListenerRegistration<?>> regs = regNode.getRegistrations();
        if (!regs.isEmpty()) {
            //通知linsteners
            notifyListeners(regs, path, candNode);
        }

        for (DataTreeCandidateNode candChild : candNode.getChildNodes()) {
            if (candChild.getModificationType() != ModificationType.UNMODIFIED) {
                //regChild是regNode向下寻找一层的孩子节点
                final RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> regChild
                  = regNode.getExactChild(candChild.getIdentifier());
                //如果child不为空，也就是说有可能有人注册监听了child节点或child节点以下的部分
                if (regChild != null) {
                    notifyNode(path.node(candChild.getIdentifier()), regChild, candChild);
                }
                //TODO: 寻找不完全匹配的node，这里用nodeIdentifier重新包装nodeWithValue和nodexxxwithPredicate的类，进行寻找
                //rc是regNode向下寻找不完全匹配的一部分，其实也是Nil或者signltonList
                //和上面的写法不同，但其实是一个意思
                for (RegistrationTreeNode<AbstractDOMDataTreeChangeListenerRegistration<?>> rc :
                  regNode.getInexactChildren(candChild.getIdentifier())) {
                    notifyNode(path.node(candChild.getIdentifier()), rc, candChild);
                }
            }
        }
    }
}
