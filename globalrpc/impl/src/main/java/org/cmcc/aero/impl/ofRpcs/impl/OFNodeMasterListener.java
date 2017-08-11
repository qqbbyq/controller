/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.base.Preconditions;
import io.netty.util.internal.ConcurrentSet;
import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipChange;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by cmcc on 2017/8/10.
 */
public class OFNodeMasterListener implements EntityOwnershipListener {
    private Logger LOG = LoggerFactory.getLogger(OFNodeMasterListener.class);
    private final String OF_NODE_ENTITY = "openflow";
    private Set<Entity> localMastered = new ConcurrentSet<>();
    private Set<Entity> otherMasterd = new ConcurrentSet<>();
    private EntityOwnershipListenerRegistration listenerRegistration;

    public OFNodeMasterListener(EntityOwnershipService entityOwnershipService){
        this.listenerRegistration = entityOwnershipService.registerListener(OF_NODE_ENTITY, this);
    }
    @Override
    public void ownershipChanged(EntityOwnershipChange ownershipChange) {
        LOG.info("{} entity ownership changed.", OF_NODE_ENTITY);
        final Entity entity = ownershipChange.getEntity();
        if (!ownershipChange.inJeopardy()) {
            if (ownershipChange.isOwner()) {
                LOG.info("OFNodeMasterListener: This node might be owner of the {}", entity);
                localMastered.add(entity);
            } else {
                LOG.info("OFNodeMasterListener: This node not owner of the {}", entity);
                if (ownershipChange.wasOwner()) {
                    localMastered.remove(entity);
                }
                if (ownershipChange.hasOwner()) {
                    otherMasterd.add(entity);
                }else {
                    if(localMastered.contains(entity))
                        otherMasterd.remove(entity);
                }
            }
        }
        return;
    }

    private Entity generateEntity(InstanceIdentifier<FlowCapableNode> nodeIdent){
        String nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue();
        Entity entity = new Entity(OF_NODE_ENTITY, nodeId);
        return entity;
    }

    private String parseNodeId(Entity entity){
        YangInstanceIdentifier entityId = entity.getId();
        Preconditions.checkArgument(entityId.getLastPathArgument() instanceof YangInstanceIdentifier.NodeWithValue,
                "EntityID must with value");
        YangInstanceIdentifier.NodeWithValue entityName = (YangInstanceIdentifier.NodeWithValue) entityId.getLastPathArgument();
        return (String) entityName.getValue();
    }

    public boolean isNodeActive(InstanceIdentifier<FlowCapableNode> nodeIdent) {
        Entity entity = generateEntity(nodeIdent);
        return localMastered.contains(entity) || otherMasterd.contains(entity);
    }

    public boolean isLocalMaster(InstanceIdentifier<FlowCapableNode> nodeIdent) {
        Entity entity = generateEntity(nodeIdent);
        return localMastered.contains(entity);
    }

    public void close() {
        this.listenerRegistration.close();
    }

    public List<String> listLocalMsterNodes() {
        List<String> localMasterdNodes = new ArrayList<>();
        for(Entity entity : localMastered){
            String nodeId = parseNodeId(entity);
            localMasterdNodes.add(nodeId);
        }
        return localMasterdNodes;
    }
}
