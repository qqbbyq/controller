/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.base.Preconditions;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by cmcc on 2017/8/10.
 */
public class OFNodeMasterListener implements EntityOwnershipListener {
    private Logger LOG = LoggerFactory.getLogger(OFNodeMasterListener.class);
    private final String OF_NODE_ENTITY = "org.opendaylight.mdsal.ServiceEntityType";
    private ConcurrentMap<String, Entity> localMastered = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Entity> otherMasterd = new ConcurrentHashMap<>();
    private EntityOwnershipListenerRegistration listenerRegistration;

    public OFNodeMasterListener(EntityOwnershipService entityOwnershipService){
        this.listenerRegistration = entityOwnershipService.registerListener(OF_NODE_ENTITY, this);
    }
    @Override
    public void ownershipChanged(EntityOwnershipChange ownershipChange) {
        LOG.info("{} entity ownership changed.", OF_NODE_ENTITY);
        final Entity entity = ownershipChange.getEntity();
        String entityId = parseNodeId(entity);
        if (!ownershipChange.inJeopardy()) {
            if (ownershipChange.isOwner()) {
                LOG.info("OFNodeMasterListener: This node might be owner of the {}", entity);
                localMastered.put(entityId, entity);
            } else {
                LOG.info("OFNodeMasterListener: This node not owner of the {}", entityId);
                if (ownershipChange.wasOwner()) {
                    localMastered.remove(entityId);
                }
                if (ownershipChange.hasOwner()) {
                    otherMasterd.put(entityId, entity);
                }else {
                    if(otherMasterd.containsKey(entityId))
                        otherMasterd.remove(entityId);
                }
            }
        }
        return;
    }

    private String generateEntityId(InstanceIdentifier<Node> nodeIdent){
        String nodeId = nodeIdent.firstKeyOf(Node.class).getId().getValue();
        LOG.info("OFNodeMasterListener.generateEntityId compute target nodeId is {}", nodeId);
        return nodeId;
    }

    private String parseNodeId(Entity entity){
        YangInstanceIdentifier entityId = entity.getId();
        Preconditions.checkArgument(entityId.getLastPathArgument() instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates,
                "EntityID must with value");
        YangInstanceIdentifier.NodeIdentifierWithPredicates entityName = (YangInstanceIdentifier.NodeIdentifierWithPredicates) entityId.getLastPathArgument();
        return (String) entityName.getKeyValues().values().iterator().next();
    }

    public boolean isNodeActive(InstanceIdentifier<Node> nodeIdent) {
        String entityId = generateEntityId(nodeIdent);
        return localMastered.containsKey(entityId) || otherMasterd.containsKey(entityId);
    }

    public boolean isLocalMaster(InstanceIdentifier<Node> nodeIdent) {
        String entityId = generateEntityId(nodeIdent);
        return localMastered.containsKey(entityId);
    }

    public void close() {
        this.listenerRegistration.close();
    }

    public List<String> listLocalMasterNodes() {
        List<String> localMasterdNodes = new ArrayList<>();
        localMasterdNodes.addAll(localMastered.keySet());
        return localMasterdNodes;
    }
}
