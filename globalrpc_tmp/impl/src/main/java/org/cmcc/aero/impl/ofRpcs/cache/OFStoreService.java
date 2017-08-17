/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;


import org.opendaylight.controller.cluster.datastore.utils.SerializationUtils;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodecFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by cmcc on 2017/3/27.
 */
public class OFStoreService {

    private static final String SPLITOR = "&";
    private RedisCacheService redisCacheService;
    private BindingToNormalizedNodeCodec codec;
    private Logger LOG = LoggerFactory.getLogger(OFStoreService.class);

    private OFStoreService(RedisCacheService redisCacheService, BindingToNormalizedNodeCodec codec){
        this.redisCacheService = redisCacheService;
        this.codec = codec;
    }

    public static OFStoreService createStoreService(RedisCacheService redisCacheService, ClassLoadingStrategy classLoadingStrategy){
        BindingToNormalizedNodeCodec codec = BindingToNormalizedNodeCodecFactory.newInstance(classLoadingStrategy);
        return new OFStoreService(redisCacheService, codec);
    }

    public String generateCachedFlowId(Short tableId, String flowId) {
        return String.valueOf(tableId).concat(SPLITOR).concat(flowId);
    }

    private byte[] getFlowBytes(InstanceIdentifier nodeIdent, Short tableId, String flowId, org.opendaylight.yang.gen.
            v1.urn.opendaylight.flow.types.rev131026.Flow flowInput) {
        InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
        FlowKey flowKey = new FlowKey(new FlowId(flowId));
        InstanceIdentifier<Flow> flowIid = flowCapableNode.child(Table.class, new TableKey(tableId)).
                child(Flow.class, flowKey);
        Flow flow = (new FlowBuilder(flowInput)).setKey(flowKey).build();
        return generateNormalizedNodeBytes(flowIid, flow);
    }

    private byte[] getGroupBytes(InstanceIdentifier nodeIdent, Long groupId, org.opendaylight.yang.gen.v1.urn.
            opendaylight.group.types.rev131018.Group groupInput) {
        InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
        GroupKey groupKey = new GroupKey(new GroupId(groupId));
        InstanceIdentifier<Group> groupIid = flowCapableNode.child(Group.class, groupKey);
        Group group = (new GroupBuilder(groupInput).setKey(groupKey)).build();
        return generateNormalizedNodeBytes(groupIid, group);
    }

    private byte[] getMeterBytes(InstanceIdentifier nodeIdent, Long meterId, org.opendaylight.yang.gen.v1.urn.opendaylight.
            meter.types.rev130918.Meter meterInput) {
        InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
        MeterKey meterKey = new MeterKey(new MeterId(meterId));
        InstanceIdentifier<Meter> meterIid = flowCapableNode.child(Meter.class, meterKey);
        Meter meter = (new MeterBuilder(meterInput).setKey(meterKey)).build();
        return generateNormalizedNodeBytes(meterIid, meter);
    }

    private <T extends DataObject> byte[] generateNormalizedNodeBytes(InstanceIdentifier<T> path, T data) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.toNormalizedNode(path, data);
        return SerializationUtils.serializeNormalizedNode(normalizedNode.getValue());
    }

    private InstanceIdentifier<FlowCapableNode> getNodeIdentifier(InstanceIdentifier nodeIdent) {
        InstanceIdentifier<Node> nodeId = (InstanceIdentifier<Node>) nodeIdent;
        InstanceIdentifier<FlowCapableNode> flowCapableNode = nodeId.augmentation(FlowCapableNode.class);
        return flowCapableNode;
    }

    public void addFlowEntry(InstanceIdentifier nodeIdent, Short tableId, String flowId, org.opendaylight.yang.gen.
            v1.urn.opendaylight.flow.types.rev131026.Flow flowInput) {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        String tableFlowId = generateCachedFlowId(tableId, flowId);
        byte[] flowBytes = getFlowBytes(nodeIdent, tableId, flowId, flowInput);
        redisCacheService.addFlowEntry(nodeId, tableFlowId, flowBytes);
    }

    public void batchRemoveFlows(InstanceIdentifier nodeIdent, List<String> removeFlowIds) {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        redisCacheService.delFlowEntries(nodeId, removeFlowIds);
    }

    public void addGroup(InstanceIdentifier nodeIdent, Long groupId, org.opendaylight.yang.gen.v1.urn.opendaylight.
            group.types.rev131018.Group groupInput) {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] groupBytes = getGroupBytes(nodeIdent, groupId, groupInput);
        redisCacheService.addGroupEntry(nodeId, String.valueOf(groupId), groupBytes);
    }

    public void batchRemovceGroups(InstanceIdentifier nodeIdent, List<Long> removedGroupIds) {

        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        redisCacheService.delGroupEntries(nodeId, removedGroupIds);
    }

    public void addMeter(InstanceIdentifier nodeIdent, Long meterId, org.opendaylight.yang.gen.v1.urn.opendaylight.
            meter.types.rev130918.Meter meterInput) {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] meterBytes = getMeterBytes(nodeIdent, meterId, meterInput);
        redisCacheService.addMeterEntry(nodeId, String.valueOf(meterId), meterBytes);
    }

    public void batchRemoveMeters(InstanceIdentifier nodeIdent, List<Long> removedMeterIds) {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        redisCacheService.delMeterEntries(nodeId, removedMeterIds);
    }
}
