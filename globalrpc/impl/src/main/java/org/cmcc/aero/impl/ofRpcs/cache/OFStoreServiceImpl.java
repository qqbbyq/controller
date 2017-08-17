/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;


import org.cmcc.aero.impl.utils.NodeNormalizedCodecUtils;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.cmcc.aero.impl.utils.ConstantsUtils.SPLITOR;

/**
 * Created by cmcc on 2017/3/27.
 */
public class OFStoreServiceImpl implements OFStoreService{
    private RedisCacheService redisCacheService;
    private NodeNormalizedCodecUtils codec;
    private Logger LOG = LoggerFactory.getLogger(OFStoreServiceImpl.class);

    private OFStoreServiceImpl(RedisCacheService redisCacheService, NodeNormalizedCodecUtils codec){
        this.redisCacheService = redisCacheService;
        this.codec = codec;
    }

    public static OFStoreServiceImpl createStoreService(RedisCacheService redisCacheService, NodeNormalizedCodecUtils codec){
        return new OFStoreServiceImpl(redisCacheService, codec);
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
        return codec.generateNormalizedNodeBytes(flowIid, flow);
    }

    private byte[] getGroupBytes(InstanceIdentifier nodeIdent, Long groupId, org.opendaylight.yang.gen.v1.urn.
            opendaylight.group.types.rev131018.Group groupInput) {
        InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
        GroupKey groupKey = new GroupKey(new GroupId(groupId));
        InstanceIdentifier<Group> groupIid = flowCapableNode.child(Group.class, groupKey);
        Group group = (new GroupBuilder(groupInput).setKey(groupKey)).build();
        return codec.generateNormalizedNodeBytes(groupIid, group);
    }

    private byte[] getMeterBytes(InstanceIdentifier nodeIdent, Long meterId, org.opendaylight.yang.gen.v1.urn.opendaylight.
            meter.types.rev130918.Meter meterInput) {
        InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
        MeterKey meterKey = new MeterKey(new MeterId(meterId));
        InstanceIdentifier<Meter> meterIid = flowCapableNode.child(Meter.class, meterKey);
        Meter meter = (new MeterBuilder(meterInput).setKey(meterKey)).build();
        return codec.generateNormalizedNodeBytes(meterIid, meter);
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

    @Override
    public String getFlowString(String nodeId, String flowId) {
        byte[] flowBytes = redisCacheService.getFlowEntry(nodeId, flowId);
        try {
            String flowString = codec.genenrateJsonString(flowBytes);
            return flowString;
        } catch (Exception e) {
            LOG.error("Exception while translate normalizednode to json.",e);
        }
        return "Exception";
    }

    @Override
    public String getGroupString(String nodeId, String groupId) {
        byte[] groupBytes = redisCacheService.getGroupEntry(nodeId, groupId);
        try {
            String groupString = codec.genenrateJsonString(groupBytes);
            return groupString;
        } catch (Exception e) {
            LOG.error("Exception while translate normalizednode to json.",e);
        }
        return "Exception";
    }

    @Override
    public String getMeterString(String nodeId, String meterId) {
        byte[] meterBytes = redisCacheService.getGroupEntry(nodeId, meterId);
        try {
            String meterString = codec.genenrateJsonString(meterBytes);
            return meterString;
        } catch (Exception e) {
            LOG.error("Exception while translate normalizednode to json.",e);
        }
        return "Exception";
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

    public Group getGroup(InstanceIdentifier nodeIdent, Long groupId){
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] groupBytes = redisCacheService.getGroupEntry(nodeId, String.valueOf(groupId));
        if(groupBytes != null){
            InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
            GroupKey groupKey = new GroupKey(new GroupId(groupId));
            InstanceIdentifier<Group> groupIid = flowCapableNode.child(Group.class, groupKey);
            Map.Entry<InstanceIdentifier<?>, DataObject> dataEntry = codec.generateDataObject(groupIid, groupBytes);
            return (Group)dataEntry.getValue();
        }
        return null;
    }

    public Flow getFlow(InstanceIdentifier nodeIdent, short tableId, String flowId){
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        String tableFlowId = generateCachedFlowId(tableId, flowId);
        byte[] flowBytes = redisCacheService.getFlowEntry(nodeId, tableFlowId);
        if(flowBytes != null){
            InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
            InstanceIdentifier<Flow> FlowIid = flowCapableNode.child(Table.class, new TableKey(tableId)).
                    child(Flow.class, new FlowKey(new FlowId(flowId)));
            Map.Entry<InstanceIdentifier<?>, DataObject> dataEntry = codec.generateDataObject(FlowIid, flowBytes);
            return (Flow)dataEntry.getValue();
        }
        return null;
    }

    public Meter getMeter(InstanceIdentifier nodeIdent, Long meterId){
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] meterBytes = redisCacheService.getMeterEntry(nodeId, String.valueOf(meterId));
        if(meterBytes != null){
            InstanceIdentifier<FlowCapableNode> flowCapableNode = getNodeIdentifier(nodeIdent);
            MeterKey meterKey = new MeterKey(new MeterId(meterId));
            InstanceIdentifier<Meter> groupIid = flowCapableNode.child(Meter.class, meterKey);
            Map.Entry<InstanceIdentifier<?>, DataObject> dataEntry = codec.generateDataObject(groupIid, meterBytes);
            return (Meter)dataEntry.getValue();
        }
        return null;
    }

    public String getFlowJson(InstanceIdentifier nodeIdent, short tableId, String flowId) throws IOException,
            URISyntaxException {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        String tableFlowId = generateCachedFlowId(tableId, flowId);
        byte[] flowBytes = redisCacheService.getFlowEntry(nodeId, tableFlowId);
        if(flowBytes != null){
            return codec.genenrateJsonString(flowBytes);
        }
        return null;
    }

    public String getGroupJson(InstanceIdentifier nodeIdent, Long groupId) throws IOException, URISyntaxException {
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] groupBytes = redisCacheService.getGroupEntry(nodeId, String.valueOf(groupId));
        if(groupBytes != null){
            return codec.genenrateJsonString(groupBytes);
        }
        return null;
    }

    public String getMeterJson(InstanceIdentifier nodeIdent, Long meterId) throws IOException,  URISyntaxException{
        NodeKey nodeKey = (NodeKey) nodeIdent.firstKeyOf(Node.class);
        String nodeId = nodeKey.getId().getValue();
        byte[] meterBytes = redisCacheService.getMeterEntry(nodeId, String.valueOf(meterId));
        if(meterBytes != null){
            return codec.genenrateJsonString(meterBytes);
        }
        return null;
    }
}
