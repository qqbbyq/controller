/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs;

import org.cmcc.aero.impl.utils.NodeNormalizedCodecUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cmcc on 2017/8/17.
 */
public class OfRpcUtils {
    private NodeNormalizedCodecUtils codec;

    public OfRpcUtils(NodeNormalizedCodecUtils codec){
        this.codec = codec;
    }

    public List<Flow> getNodeFlows(Node node) {
        List<Table> relatedTables = node.getAugmentation(FlowCapableNode.class).getTable();
        List<Flow> targetFlows = new ArrayList<>();
        for(Table table : relatedTables){
            targetFlows.addAll(table.getFlow());
        }
        return targetFlows;
    }

    public Map.Entry<InstanceIdentifier,List<Flow>> getNodeFlowMap(YangInstanceIdentifier nodeYangId,
                                                                   NormalizedNode normalNode) {
        Map.Entry<InstanceIdentifier<?>, DataObject> nodeMap = codec.generateDataObject(nodeYangId, normalNode);
        InstanceIdentifier nodeIdent = nodeMap.getKey();
        Node node = (Node) nodeMap.getValue();
        List<Flow> nodeFlows = getNodeFlows(node);
        if(nodeIdent != null && nodeFlows!=null){
            return new AbstractMap.SimpleEntry<>(nodeIdent, nodeFlows);
        }
        return null;
    }

    private Node createNormalFlowNode(String nodeIdStr, List<Flow> flows) {
        NodeId nodeId = new NodeId(nodeIdStr);
        NodeBuilder nodeBuilder = new NodeBuilder().setId(nodeId).setKey(new NodeKey(nodeId));
        FlowCapableNodeBuilder flowCapableNodeBuilder = new FlowCapableNodeBuilder();
        flowCapableNodeBuilder.setTable(new ArrayList<>());
        Map<Short, List<Flow>> tableMap = new HashMap<>();
        for(Flow flow : flows){
            Short tableId = flow.getTableId();
            if(!tableMap.containsKey(tableId)){
                tableMap.put(tableId, new ArrayList<>());
            }
            tableMap.get(tableId).add(flow);
        }
        for(Map.Entry<Short, List<Flow>> entry : tableMap.entrySet()){
            TableBuilder tableBuilder = new TableBuilder().setId(entry.getKey()).setKey(new TableKey(entry.getKey()))
                    .setFlow(entry.getValue());
            flowCapableNodeBuilder.getTable().add(tableBuilder.build());
        }
        nodeBuilder.addAugmentation(FlowCapableNode.class, flowCapableNodeBuilder.build());
        return nodeBuilder.build();
    }

    public InstanceIdentifier<Node> createNodeId(String nodeId) {
        return InstanceIdentifier.create(Nodes.class).
                child(Node.class, new NodeKey(new NodeId(nodeId)));
    }

    public Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> makeFlowNodeNormal(String nodeId, List<Flow> flows) {
        InstanceIdentifier<Node> nodeIid = createNodeId(nodeId);
        Node flowNode = createNormalFlowNode(nodeId, flows);
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.generateNormalizedNode(
                nodeIid, flowNode);
        return normalizedNode;
    }

    public Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> makeMeterNodeNormal(String nodeId, List<Meter> meters) {
        InstanceIdentifier<Node> nodeIid = createNodeId(nodeId);
        Node meterNode = createNormalMeterNode(nodeId, meters);
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.generateNormalizedNode(
                nodeIid, meterNode);
        return normalizedNode;
    }

    private Node createNormalMeterNode(String nodeIdStr, List<Meter> meters) {
        NodeId nodeId = new NodeId(nodeIdStr);
        NodeBuilder nodeBuilder = new NodeBuilder().setId(nodeId).setKey(new NodeKey(nodeId));
        FlowCapableNodeBuilder flowCapableNodeBuilder = new FlowCapableNodeBuilder();
        flowCapableNodeBuilder.setMeter(meters);
        nodeBuilder.addAugmentation(FlowCapableNode.class, flowCapableNodeBuilder.build());
        return nodeBuilder.build();
    }

    public Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> makeGroupNodeNormal(String nodeId, List<Group> groups) {
        InstanceIdentifier<Node> nodeIid = createNodeId(nodeId);
        Node meterNode = createNormalGroupNode(nodeId, groups);
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.generateNormalizedNode(
                nodeIid, meterNode);
        return normalizedNode;
    }

    private Node createNormalGroupNode(String nodeIdStr, List<Group> groups) {
        NodeId nodeId = new NodeId(nodeIdStr);
        NodeBuilder nodeBuilder = new NodeBuilder().setId(nodeId).setKey(new NodeKey(nodeId));
        FlowCapableNodeBuilder flowCapableNodeBuilder = new FlowCapableNodeBuilder();
        flowCapableNodeBuilder.setGroup(groups);
        nodeBuilder.addAugmentation(FlowCapableNode.class, flowCapableNodeBuilder.build());
        return nodeBuilder.build();
    }

    public Map.Entry<InstanceIdentifier, List<Meter>> getNodeMeterMap(YangInstanceIdentifier nodeYangId, NormalizedNode normalNode) {
        Map.Entry<InstanceIdentifier<?>, DataObject> nodeMap = codec.generateDataObject(nodeYangId, normalNode);
        InstanceIdentifier nodeIdent = nodeMap.getKey();
        Node node = (Node) nodeMap.getValue();
        List<Meter> nodeMeters = getNodeMeters(node);
        if(nodeIdent != null && nodeMeters!=null){
            return new AbstractMap.SimpleEntry<>(nodeIdent, nodeMeters);
        }
        return null;
    }

    private List<Meter> getNodeMeters(Node node) {
        List<Meter> relatedMeters = node.getAugmentation(FlowCapableNode.class).getMeter();
        return relatedMeters;
    }

    public Map.Entry<InstanceIdentifier, List<Group>> getNodeGroupMap(YangInstanceIdentifier nodeYangId, NormalizedNode normalNode) {
        Map.Entry<InstanceIdentifier<?>, DataObject> nodeMap = codec.generateDataObject(nodeYangId, normalNode);
        InstanceIdentifier nodeIdent = nodeMap.getKey();
        Node node = (Node) nodeMap.getValue();
        List<Group> nodeMeters = getNodeGroup(node);
        if(nodeIdent != null && nodeMeters!=null){
            return new AbstractMap.SimpleEntry<>(nodeIdent, nodeMeters);
        }
        return null;
    }

    private List<Group> getNodeGroup(Node node) {
        List<Group> relatedMeters = node.getAugmentation(FlowCapableNode.class).getGroup();
        return relatedMeters;
    }
}
