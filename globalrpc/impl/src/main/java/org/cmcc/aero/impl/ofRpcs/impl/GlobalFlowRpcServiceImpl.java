/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.collect.Lists;
import org.cmcc.aero.impl.ofRpcs.OfRpcUtils;
import org.cmcc.aero.impl.ofRpcs.api.GlobalFlowRpcService;
import org.cmcc.aero.impl.ofRpcs.cache.OFStoreService;
import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.cmcc.aero.impl.utils.ConstantsUtils.SPLITOR;

/**
 * Created by cmcc on 2017/8/8.
 */
public class GlobalFlowRpcServiceImpl implements GlobalFlowRpcService {
    private OFFwdServiceImpl delegateSvc;
    private OFStoreService cacheService;
    private OfRpcUtils ofRpcUtils;
    private GlobalRpcClient globalRpcClient;
    private AtomicLong txLong = new AtomicLong(0);
    private Logger LOG = LoggerFactory.getLogger(GlobalFlowRpcService.class);

    public GlobalFlowRpcServiceImpl(OFFwdServiceImpl openflowFwdingSvc, GlobalRpcClient globalRpcClient,
                                    OFStoreService cacheService, OfRpcUtils ofRpcUtils){
        this.delegateSvc =openflowFwdingSvc;
        this.globalRpcClient = globalRpcClient;
        this.cacheService = cacheService;
        this.ofRpcUtils = ofRpcUtils;
        initService();
    }

    private void initService() {
        String ofFwdServiceName = delegateSvc.getServiceName();
        globalRpcClient.register(delegateSvc, ofFwdServiceName, ofFwdServiceName);
    }

    private String locateOFNode(InstanceIdentifier<Node> nodeIdent){
        return globalRpcClient.locate(delegateSvc.getServiceName(), delegateSvc.getServiceName(), nodeIdent,
                GlobalRpcClient.Scale.CLUSTER);
    }

    public Future<GlobalRpcResult> removeFlowsBatch(String nodeId, List<Flow> flows){

        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeFlowNodeNormal(nodeId, flows);
        if(normalizedNode!=null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            String resourecePath = locateOFNode(nodeIid);
            String transactionId = generateTxId();
            Future<GlobalRpcResult> future = globalRpcClient.globalCall(transactionId, resourecePath,
                    "removeFlowsBatch", normalizedNode.getKey(), normalizedNode.getValue());

            removeFlowsFromCache(nodeIid, flows);
            return future;
        }else{
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void removeFlowsFromCache(InstanceIdentifier targetNode, List<Flow> flows) {
        List<String> removeFlowIds = Lists.newArrayList();
        for(Flow flow : flows){
            Short tableId = flow.getTableId();
            String flowId = flow.getId().getValue();
            String cachedFlowId = String.valueOf(tableId).concat(SPLITOR).concat(flowId);;
            removeFlowIds.add(cachedFlowId);
        }
        cacheService.batchRemoveFlows(targetNode, removeFlowIds);
    }

    private synchronized String generateTxId() {
        return String.valueOf(this.getClass().getSimpleName()) + "-" + txLong.incrementAndGet();
    }

    public Future<GlobalRpcResult> addFlowsBatch(String nodeId, List<Flow> flows){
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeFlowNodeNormal(nodeId, flows);
        if(normalizedNode!= null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            addFlows2Cache(nodeIid, flows);
            String resourecePath = locateOFNode(nodeIid);
            String transactionId = generateTxId();

            // Only for test, not use globalcall
//            delegateSvc.addFlowsBatch(normalizedNode.getKey(), normalizedNode.getValue());
            return globalRpcClient.globalCall(transactionId, resourecePath, "addFlowsBatch", normalizedNode.getKey(),
              normalizedNode.getValue());
        }else{
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void addFlows2Cache(InstanceIdentifier nodeIdent, List<Flow> flows) {
        for(Flow flow : flows){
            Short tableId = flow.getTableId();
            String flowId = flow.getId().getValue();
            cacheService.addFlowEntry(nodeIdent, tableId, flowId, flow);
        }
    }

    public Future<GlobalRpcResult> removeMetersBatch(RemoveMetersBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        Future<GlobalRpcResult> future = globalRpcClient.globalCall(transactionId, resourecePath,
                "removeMetersBatch", input);
        removeMetersFromCache(input);
        return future;
    }

    private void removeMetersFromCache(RemoveMetersBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        List<Long> removedMeterIds = Lists.newArrayList();
        for(BatchRemoveMeters batchMeter : input.getBatchRemoveMeters()){
            Long meterId = batchMeter.getMeterId().getValue();
            removedMeterIds.add(meterId);
        }
        cacheService.batchRemoveMeters(targetNode, removedMeterIds);
    }

    public Future<GlobalRpcResult> updateMetersBatch(UpdateMetersBatchInput input) {
        updateMetersFromCache(input);
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "updateMetersBatch", input);
    }

    private void updateMetersFromCache(UpdateMetersBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        for(BatchUpdateMeters batchedMeters : input.getBatchUpdateMeters()){
            OriginalBatchedMeter originalMeter = batchedMeters.getOriginalBatchedMeter();
            Long meterId = originalMeter.getMeterId().getValue();
            UpdatedBatchedMeter updatedMeter = batchedMeters.getUpdatedBatchedMeter();
            cacheService.addMeter(targetNode, meterId, updatedMeter);
        }
    }

    public Future<GlobalRpcResult>  addMetersBatch(AddMetersBatchInput input) {
        addMeters2Cache(input);
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "addMetersBatch", input);
    }

    private void addMeters2Cache(AddMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        for(BatchAddMeters meter : input.getBatchAddMeters()){
            Long meterId = meter.getMeterId().getValue();
            cacheService.addMeter(nodeIdent, meterId, meter);
        }
    }

    public Future<GlobalRpcResult> addGroupsBatch(AddGroupsBatchInput input) {
        addGroups2Cache(input);
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "addGroupsBatch", input);
    }

    private void addGroups2Cache(AddGroupsBatchInput input) {
        InstanceIdentifier node = input.getNode().getValue();
        for(BatchAddGroups batchGroups : input.getBatchAddGroups()){
            Long groupId = batchGroups.getGroupId().getValue();
            cacheService.addGroup(node, groupId, batchGroups);
        }
    }

    public Future<GlobalRpcResult>  removeGroupsBatch(RemoveGroupsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        Future<GlobalRpcResult> future = globalRpcClient.globalCall(transactionId, resourecePath, "removeGroupsBatch", input);
        removeGroupsFromCache(input);
        return future;
    }

    private void removeGroupsFromCache(RemoveGroupsBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        List<Long> removedGroupIds = Lists.newArrayList();
        for(BatchRemoveGroups batchedGroups : input.getBatchRemoveGroups()){
            removedGroupIds.add(batchedGroups.getGroupId().getValue());
        }
        cacheService.batchRemovceGroups(targetNode, removedGroupIds);
    }

    public Future<GlobalRpcResult>  updateGroupsBatch(UpdateGroupsBatchInput input) {
        updateGroupsFromCache(input);
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "updateGroupsBatch", input);
    }

    private void updateGroupsFromCache(UpdateGroupsBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        for(BatchUpdateGroups batchedGroups : input.getBatchUpdateGroups()){
            OriginalBatchedGroup originalGroup = batchedGroups.getOriginalBatchedGroup();
            Long groupId = originalGroup.getGroupId().getValue();
            UpdatedBatchedGroup updatedGroup = batchedGroups.getUpdatedBatchedGroup();
            cacheService.addGroup(targetNode, groupId, updatedGroup);
        }
    }

    @Override
    public void close() {
        this.delegateSvc.close();
    }

    @Override
    public List<String> listLocalMasteredNodes() {
        return delegateSvc.listLocalMasteredNodes();
    }

    @Override
    public String getFlowString(String nodeId, String flowId) {
        return cacheService.getFlowString(nodeId, flowId);
    }

    @Override
    public String getGroupString(String nodeId, String groupId) {
        return cacheService.getGroupString(nodeId, groupId);
    }

    @Override
    public String getMeterString(String nodeId, String meterId) {
        return cacheService.getMeterString(nodeId, meterId);
    }
}
