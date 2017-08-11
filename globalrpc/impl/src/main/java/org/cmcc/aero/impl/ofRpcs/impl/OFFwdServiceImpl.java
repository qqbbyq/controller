/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;

import org.cmcc.aero.impl.ofRpcs.api.OFFwdService;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.cmcc.aero.impl.ofRpcs.cache.OFStoreService;
import org.opendaylight.openflowplugin.impl.services.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMetersBatchServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.BatchFlowInputGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.OriginalBatchedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.OriginalBatchedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.input.update.grouping.UpdatedBatchedGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.update.groups.batch.input.BatchUpdateGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/7/14.
 */
public class OFFwdServiceImpl implements OFFwdService, GlobalRpcIntf {

    private SalFlowsBatchService delegateFlowBatchSvc;
    private SalGroupsBatchService delegateGroupBatchSvc;
    private SalMetersBatchService delegateMeterBatchSvc;
    private OFNodeMasterListener ofNodeOwnerManager;
    private OFStoreService cacheService;

    private OFFwdServiceImpl(OFNodeMasterListener ofMaster, OFStoreService cacheService,
                             SalFlowsBatchService flowsBatchService, SalGroupsBatchService groupsBatchService,
                             SalMetersBatchService metersBatchService){
        this.ofNodeOwnerManager = ofMaster;
        this.cacheService = cacheService;
        this.delegateFlowBatchSvc = flowsBatchService;
        this.delegateGroupBatchSvc = groupsBatchService;
        this.delegateMeterBatchSvc = metersBatchService;
    }

    public String getServiceName(){
        return OFFwdService.class.getName();
    }

    public String getServiceType(){
        return getServiceName();
    }

    public static OFFwdService createInstance(OFNodeMasterListener ofMaster, OFStoreService cacheService,
                                              SalFlowsBatchService flowsBatchService,
                                              SalGroupsBatchService groupsBatchService,
                                              SalMetersBatchService metersBatchService){

        return new OFFwdServiceImpl(ofMaster, cacheService, flowsBatchService, groupsBatchService, metersBatchService);
    }

    public static SalFlowsBatchService createFlowsBatchService(RpcProviderRegistry rpcProviderRegistry,
                                                                 SalFlowService salFlowService,
                                                                 FlowCapableTransactionService flowCapableTransactionService){
        SalFlowsBatchServiceImpl salFlowsBatchService = new SalFlowsBatchServiceImpl(salFlowService,
                flowCapableTransactionService);
        rpcProviderRegistry.addRpcImplementation(SalFlowsBatchService.class, salFlowsBatchService);
        return salFlowsBatchService;
    }

    public static SalGroupsBatchService createGroupsBatchService(RpcProviderRegistry rpcProviderRegistry,
                                                                 SalGroupService salGroupService,
                                                                 FlowCapableTransactionService flowCapableTransactionService){
        SalGroupsBatchServiceImpl salGroupsBatchService = new SalGroupsBatchServiceImpl(salGroupService,
                flowCapableTransactionService);
        rpcProviderRegistry.addRpcImplementation(SalGroupsBatchService.class, salGroupsBatchService);
        return salGroupsBatchService;
    }

    public static SalMetersBatchService createMetersBatchService(RpcProviderRegistry rpcProviderRegistry,
                                                                    SalMeterService salMeterService,
                                                                    FlowCapableTransactionService flowCapableTransactionService){
        SalMetersBatchServiceImpl salMetersBatchService = new SalMetersBatchServiceImpl(salMeterService,
                flowCapableTransactionService);
        rpcProviderRegistry.addRpcImplementation(SalMetersBatchService.class, salMetersBatchService);
        return salMetersBatchService;
    }

    @Override
    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(RemoveFlowsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            removeFlowsFromCache(input);
            return delegateFlowBatchSvc.removeFlowsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void removeFlowsFromCache(RemoveFlowsBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        List<String> removeFlowIds = Lists.newArrayList();
        for(BatchFlowInputGrouping batchedFlows : input.getBatchRemoveFlows()){
            Short tableId = batchedFlows.getTableId();
            String flowId = batchedFlows.getFlowId().getValue();
            String cachedFlowId = cacheService.generateCachedFlowId(tableId, flowId);
            removeFlowIds.add(cachedFlowId);
        }
        cacheService.batchRemoveFlows(targetNode, removeFlowIds);
    }

    public <T> Future<RpcResult<T>> notMasterNodeRpcError() {
        SettableFuture<RpcResult<T>> rpcResult =  SettableFuture.create();
        RpcResultBuilder<T> rpcResultBld = RpcResultBuilder.<T>failed()
                .withRpcErrors(Collections.singleton(
                        RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, null, "Not node master")
                ));
        rpcResult.set(rpcResultBld.build());
        return rpcResult;
    }

    public Future<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(UpdateFlowsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            updateFlowsFromCache(input);
            return delegateFlowBatchSvc.updateFlowsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void updateFlowsFromCache(UpdateFlowsBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        for(BatchUpdateFlows batchedFlows : input.getBatchUpdateFlows()){
            OriginalBatchedFlow originalFlow = batchedFlows.getOriginalBatchedFlow();
            Short tableId = originalFlow.getTableId();
            String flowId = batchedFlows.getFlowId().getValue();
            UpdatedBatchedFlow updatedFlow= batchedFlows.getUpdatedBatchedFlow();
            cacheService.addFlowEntry(targetNode, tableId, flowId, updatedFlow);
        }
    }

    @Override
    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(AddFlowsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            addFlows2Cache(input);
            return delegateFlowBatchSvc.addFlowsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void addFlows2Cache(AddFlowsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        for(BatchFlowInputGrouping flow : input.getBatchAddFlows()){
            Short tableId = flow.getTableId();
            String flowId = flow.getFlowId().getValue();
            cacheService.addFlowEntry(nodeIdent, tableId, flowId, flow);
        }
    }

    @Override
    public Future<RpcResult<RemoveMetersBatchOutput>> removeMetersBatch(RemoveMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            removeMetersFromCache(input);
            return delegateMeterBatchSvc.removeMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
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

    @Override
    public Future<RpcResult<UpdateMetersBatchOutput>> updateMetersBatch(UpdateMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            updateMetersFromCache(input);
            return delegateMeterBatchSvc.updateMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
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

    @Override
    public Future<RpcResult<AddMetersBatchOutput>> addMetersBatch(AddMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            addMeters2Cache(input);
            return delegateMeterBatchSvc.addMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void addMeters2Cache(AddMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        for(BatchAddMeters meter : input.getBatchAddMeters()){
            Long meterId = meter.getMeterId().getValue();
            cacheService.addMeter(nodeIdent, meterId, meter);
        }
    }

    @Override
    public Future<RpcResult<AddGroupsBatchOutput>> addGroupsBatch(AddGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            addGroups2Cache(input);
            return delegateGroupBatchSvc.addGroupsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void addGroups2Cache(AddGroupsBatchInput input) {
        InstanceIdentifier node = input.getNode().getValue();
        for(BatchAddGroups batchGroups : input.getBatchAddGroups()){
            Long groupId = batchGroups.getGroupId().getValue();
            cacheService.addGroup(node, groupId, batchGroups);
        }
    }

    @Override
    public Future<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBatch(RemoveGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            removeGroupsFromCache(input);
            return delegateGroupBatchSvc.removeGroupsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    private void removeGroupsFromCache(RemoveGroupsBatchInput input) {
        InstanceIdentifier targetNode = input.getNode().getValue();
        List<Long> removedGroupIds = Lists.newArrayList();
        for(BatchRemoveGroups batchedGroups : input.getBatchRemoveGroups()){
            removedGroupIds.add(batchedGroups.getGroupId().getValue());
        }
        cacheService.batchRemovceGroups(targetNode, removedGroupIds);
    }

    @Override
    public Future<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBatch(UpdateGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            updateGroupsFromCache(input);
            return delegateGroupBatchSvc.updateGroupsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public void close() {
        ofNodeOwnerManager.close();
    }

    @Override
    public List<String> listLocalMasteredNodes() {
        return ofNodeOwnerManager.listLocalMsterNodes();
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

    private boolean preConfigurationCheck(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        Preconditions.checkNotNull(nodeIdent, "FlowCapableNode identifier can not be null!");
        return ofNodeOwnerManager.isNodeActive(nodeIdent);
    }

    @Override
    public boolean isResourceLocal(Object resourceId) {
        Preconditions.checkArgument(resourceId instanceof InstanceIdentifier, "Target resources should provide" +
                "InstanceIdentifier Path");
        InstanceIdentifier<FlowCapableNode> nodeIdent = (InstanceIdentifier<FlowCapableNode>) resourceId;
        return ofNodeOwnerManager.isLocalMaster(nodeIdent);
    }

}
