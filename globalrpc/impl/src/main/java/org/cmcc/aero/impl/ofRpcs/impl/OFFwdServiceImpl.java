/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.cmcc.aero.impl.ofRpcs.OfRpcUtils;
import org.cmcc.aero.impl.ofRpcs.api.OFFwdService;
import org.cmcc.aero.impl.ofRpcs.serialize.BatchFlowResults;
import org.cmcc.aero.impl.ofRpcs.serialize.BatchGroupResults;
import org.cmcc.aero.impl.ofRpcs.serialize.BatchMeterResults;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.impl.services.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMetersBatchServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.SalFlowsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.add.groups.batch.input.BatchAddGroupsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.remove.groups.batch.input.BatchRemoveGroupsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/7/14.
 */
public class OFFwdServiceImpl implements OFFwdService, GlobalRpcIntf {

    private SalFlowsBatchService delegateFlowBatchSvc;
    private SalGroupsBatchService delegateGroupBatchSvc;
    private SalMetersBatchService delegateMeterBatchSvc;
    private OFNodeMasterListener ofNodeOwnerManager;
    private OfRpcUtils ofRpcUtils;
    private Logger LOG = LoggerFactory.getLogger(OFFwdServiceImpl.class);

    private OFFwdServiceImpl(OFNodeMasterListener ofMaster,SalFlowsBatchService flowsBatchService,
                             SalGroupsBatchService groupsBatchService, SalMetersBatchService metersBatchService,
                             OfRpcUtils ofRpcUtils){
        this.ofNodeOwnerManager = ofMaster;
        this.delegateFlowBatchSvc = flowsBatchService;
        this.delegateGroupBatchSvc = groupsBatchService;
        this.delegateMeterBatchSvc = metersBatchService;
        this.ofRpcUtils = ofRpcUtils;
    }

    public String getServiceName(){
        return OFFwdService.class.getName();
    }

    public String getServiceType(){
        return getServiceName();
    }

    public static OFFwdService createInstance(OFNodeMasterListener ofMaster,
                                              SalFlowsBatchService flowsBatchService,
                                              SalGroupsBatchService groupsBatchService,
                                              SalMetersBatchService metersBatchService,
                                              OfRpcUtils ofRpcUtils){

        return new OFFwdServiceImpl(ofMaster, flowsBatchService, groupsBatchService, metersBatchService, ofRpcUtils);
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
    public Future<RpcResult<BatchFlowResults>> removeFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                NormalizedNode normalNode) {

        return removeFlowsBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchFlowResults>> removeFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                 NormalizedNode normalNode, boolean isBarrierAfter) {
        Map.Entry<InstanceIdentifier, List<Flow>> nodeFlowEntry = ofRpcUtils.getNodeFlowMap(nodeYangId, normalNode);
        if(nodeFlowEntry != null){
            InstanceIdentifier nodeIdent = nodeFlowEntry.getKey();
            if(preConfigurationCheck(nodeIdent)){
                List<Flow> targetFlows = nodeFlowEntry.getValue();
                RemoveFlowsBatchInputBuilder inputBuilder = new RemoveFlowsBatchInputBuilder().
                        setNode(new NodeRef(nodeIdent)).setBarrierAfter(isBarrierAfter);
                List<BatchRemoveFlows> batchRemoveFlows = new ArrayList<>();
                for(Flow flow : targetFlows){
                    BatchRemoveFlows batchRemoveFlow = new BatchRemoveFlowsBuilder(flow).setFlowId(flow.getId()).
                            setKey(new BatchRemoveFlowsKey(flow.getId())).setTableId(flow.getTableId()).build();
                    batchRemoveFlows.add(batchRemoveFlow);
                }
                inputBuilder.setBatchRemoveFlows(batchRemoveFlows);
                Future<RpcResult<RemoveFlowsBatchOutput>> future = delegateFlowBatchSvc.removeFlowsBatch(inputBuilder.build());
                CompletableFuture<RpcResult<BatchFlowResults>> flowOperResult = new CompletableFuture<>();
                if(future instanceof ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<RemoveFlowsBatchOutput>>) future), new
                            FutureCallback<RpcResult<RemoveFlowsBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<RemoveFlowsBatchOutput> removeFlowsBatchOutputRpcResult) {
                                    if(removeFlowsBatchOutputRpcResult.isSuccessful()){
                                        flowOperResult.complete(RpcResultBuilder.success(BatchFlowResults.success()).build());
                                    }else {
                                        BatchFlowResults results = new BatchFlowResults(removeFlowsBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchFlowResults> resultsRpcResult = RpcResultBuilder.failed();
                                        flowOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchFlowResults results = new BatchFlowResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchFlowResults> resultsRpcResult = RpcResultBuilder.failed();
                                    flowOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchFlowResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    flowOperResult.complete(rpcResultBuilder.build());
                }

                return flowOperResult;
            }
        }
        BatchFlowResults results = new BatchFlowResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    public <T> Future<RpcResult<T>> notMasterNodeRpcError(T result) {
        SettableFuture<RpcResult<T>> rpcResult =  SettableFuture.create();
        RpcResultBuilder<T> rpcResultBld = RpcResultBuilder.<T>failed();
        rpcResultBld.withResult(result).withRpcErrors(Collections.singleton(
                        RpcResultBuilder.newError(RpcError.ErrorType.APPLICATION, null, "Not node master")
                ));
        rpcResult.set(rpcResultBld.build());
        return rpcResult;
    }

    @Override
    public Future<RpcResult<BatchFlowResults>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                             NormalizedNode normalNode) {
        return addFlowsBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchFlowResults>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                              NormalizedNode normalNode, boolean isBarrierAfter) {
        Map.Entry<InstanceIdentifier, List<Flow>> nodeFlowEntry = ofRpcUtils.getNodeFlowMap(nodeYangId, normalNode);

        if(nodeFlowEntry != null){
            InstanceIdentifier nodeIdent = nodeFlowEntry.getKey();
            if(preConfigurationCheck(nodeIdent)){
                AddFlowsBatchInputBuilder inputBuilder = new AddFlowsBatchInputBuilder().
                        setNode(new NodeRef(nodeIdent)).setBarrierAfter(isBarrierAfter);
                List<BatchAddFlows> batchAddFlows = new ArrayList<>();
                List<Flow> targetFlows = nodeFlowEntry.getValue();
                for(Flow flow : targetFlows){
                    BatchAddFlows batchAddFlow = new BatchAddFlowsBuilder(flow).setFlowId(flow.getId()).
                            setKey(new BatchAddFlowsKey(flow.getId())).setTableId(flow.getTableId()).build();
                    batchAddFlows.add(batchAddFlow);
                }
                inputBuilder.setBatchAddFlows(batchAddFlows);
                Future<RpcResult<AddFlowsBatchOutput>> future = delegateFlowBatchSvc.addFlowsBatch(inputBuilder.build());
                CompletableFuture<RpcResult<BatchFlowResults>> flowOperResult = new CompletableFuture<>();
                if ( future instanceof ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<AddFlowsBatchOutput>>) future), new
                            FutureCallback<RpcResult<AddFlowsBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<AddFlowsBatchOutput> removeFlowsBatchOutputRpcResult) {
                                    if(removeFlowsBatchOutputRpcResult.isSuccessful()){
                                        flowOperResult.complete(RpcResultBuilder.success(BatchFlowResults.success()).build());
                                    }else {
                                        BatchFlowResults results = new BatchFlowResults(removeFlowsBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchFlowResults> resultsRpcResult = RpcResultBuilder.failed();
                                        flowOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchFlowResults results = new BatchFlowResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchFlowResults> resultsRpcResult = RpcResultBuilder.failed();
                                    flowOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchFlowResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    flowOperResult.complete(rpcResultBuilder.build());
                }
                return flowOperResult;
            }
        }
        BatchFlowResults results = new BatchFlowResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    @Override
    public Future<RpcResult<BatchMeterResults>> removeMetersBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode) {
        return removeMetersBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchMeterResults>> removeMetersBatch(YangInstanceIdentifier nodeYangId,
                                                                   NormalizedNode normalNode, boolean isBarrierAfter) {
        Map.Entry<InstanceIdentifier, List<Meter>> nodeMeterEntry = ofRpcUtils.getNodeMeterMap(nodeYangId,
                normalNode);

        if(nodeMeterEntry != null){
            InstanceIdentifier nodeIdent = nodeMeterEntry.getKey();

            if(preConfigurationCheck(nodeIdent)){
                RemoveMetersBatchInputBuilder input = new RemoveMetersBatchInputBuilder();
                input.setNode(new NodeRef(nodeIdent)).setBarrierAfter(false);
                List<BatchRemoveMeters> batchAddMetersList = new ArrayList<>();
                for(Meter meter : nodeMeterEntry.getValue()){
                    MeterId meterId = meter.getMeterId();
                    BatchRemoveMetersBuilder batchRemoveMeters = new BatchRemoveMetersBuilder(meter).setMeterId(meterId).
                            setKey(new BatchRemoveMetersKey(meterId));
                    batchAddMetersList.add(batchRemoveMeters.build());
                }
                input.setBatchRemoveMeters(batchAddMetersList);
                Future<RpcResult<RemoveMetersBatchOutput>> future = delegateMeterBatchSvc.removeMetersBatch(input.build());
                CompletableFuture<RpcResult<BatchMeterResults>> meterOperResult = new CompletableFuture<>();
                if(future instanceof ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<RemoveMetersBatchOutput>>) future), new
                            FutureCallback<RpcResult<RemoveMetersBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<RemoveMetersBatchOutput> removeMetersBatchOutputRpcResult) {
                                    if(removeMetersBatchOutputRpcResult.isSuccessful()){
                                        meterOperResult.complete(RpcResultBuilder.success(BatchMeterResults.success()).build());
                                    }else {
                                        BatchMeterResults results = new BatchMeterResults(removeMetersBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchMeterResults> resultsRpcResult = RpcResultBuilder.failed();
                                        meterOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchMeterResults results = new BatchMeterResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchMeterResults> resultsRpcResult = RpcResultBuilder.failed();
                                    meterOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchMeterResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    meterOperResult.complete(rpcResultBuilder.build());
                }
                return meterOperResult;
            }
        }
        BatchMeterResults results = new BatchMeterResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    @Override
    public Future<RpcResult<BatchMeterResults>> addMetersBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode) {
        return addMetersBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchMeterResults>> addMetersBatch(YangInstanceIdentifier nodeYangId,
                                                                NormalizedNode normalNode, boolean isBarrierAfter) {
        Map.Entry<InstanceIdentifier, List<Meter>> nodeMeterEntry = ofRpcUtils.getNodeMeterMap(nodeYangId,
                normalNode);

        if(nodeMeterEntry != null){
            InstanceIdentifier nodeIdent = nodeMeterEntry.getKey();

            if(preConfigurationCheck(nodeIdent)){
                AddMetersBatchInputBuilder input = new AddMetersBatchInputBuilder();
                input.setNode(new NodeRef(nodeIdent)).setBarrierAfter(isBarrierAfter);
                List<BatchAddMeters> batchAddMetersList = new ArrayList<>();
                for(Meter meter : nodeMeterEntry.getValue()){
                    MeterId meterId = meter.getMeterId();
                    BatchAddMetersBuilder batchAddMeters = new BatchAddMetersBuilder(meter).setMeterId(meterId).
                            setKey(new BatchAddMetersKey(meterId));
                    batchAddMetersList.add(batchAddMeters.build());
                }
                input.setBatchAddMeters(batchAddMetersList);
                Future<RpcResult<AddMetersBatchOutput>> future = delegateMeterBatchSvc.addMetersBatch(input.build());
                CompletableFuture<RpcResult<BatchMeterResults>> meterOperResult = new CompletableFuture<>();
                if (future instanceof ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<AddMetersBatchOutput>>) future), new
                            FutureCallback<RpcResult<AddMetersBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<AddMetersBatchOutput> addMetersBatchOutputRpcResult) {
                                    if(addMetersBatchOutputRpcResult.isSuccessful()){
                                        meterOperResult.complete(RpcResultBuilder.success(BatchMeterResults.success()).build());
                                    }else {
                                        BatchMeterResults results = new BatchMeterResults(addMetersBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchMeterResults> resultsRpcResult = RpcResultBuilder.failed();
                                        meterOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchMeterResults results = new BatchMeterResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchMeterResults> resultsRpcResult = RpcResultBuilder.failed();
                                    meterOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchMeterResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    meterOperResult.complete(rpcResultBuilder.build());
                }

                return meterOperResult;
            }
        }
        BatchMeterResults results = new BatchMeterResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    @Override
    public Future<RpcResult<BatchGroupResults>> addGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode) {
        return addGroupsBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchGroupResults>> addGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                                NormalizedNode normalNode, boolean isBarrierAfter) {

        Map.Entry<InstanceIdentifier, List<Group>> nodeGroupEntry = ofRpcUtils.getNodeGroupMap(nodeYangId,
                normalNode);

        if(nodeGroupEntry != null){
            InstanceIdentifier nodeIdent = nodeGroupEntry.getKey();

            if(preConfigurationCheck(nodeIdent)){
                AddGroupsBatchInputBuilder input = new AddGroupsBatchInputBuilder();
                input.setNode(new NodeRef(nodeIdent)).setBarrierAfter(isBarrierAfter);
                List<BatchAddGroups> batchAddGroupsList = new ArrayList<>();
                for(Group group : nodeGroupEntry.getValue()){
                    GroupId groupId = group.getGroupId();
                    BatchAddGroupsBuilder batchAddGroups = new BatchAddGroupsBuilder(group).setGroupId(groupId).
                            setGroupType(group.getGroupType()).setKey(new BatchAddGroupsKey(groupId));
                    batchAddGroupsList.add(batchAddGroups.build());
                }
                input.setBatchAddGroups(batchAddGroupsList);
                Future<RpcResult<AddGroupsBatchOutput>> future = delegateGroupBatchSvc.addGroupsBatch(input.build());
                CompletableFuture<RpcResult<BatchGroupResults>> groupOperResult = new CompletableFuture<>();
                if(future instanceof ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<AddGroupsBatchOutput>>) future), new
                            FutureCallback<RpcResult<AddGroupsBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<AddGroupsBatchOutput> addGroupsBatchOutputRpcResult) {
                                    if(addGroupsBatchOutputRpcResult.isSuccessful()){
                                        groupOperResult.complete(RpcResultBuilder.success(BatchGroupResults.success()).build());
                                    }else {
                                        BatchGroupResults results = new BatchGroupResults(addGroupsBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchGroupResults> resultsRpcResult = RpcResultBuilder.failed();
                                        groupOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchGroupResults results = new BatchGroupResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchGroupResults> resultsRpcResult = RpcResultBuilder.failed();
                                    groupOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchGroupResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    groupOperResult.complete(rpcResultBuilder.build());
                }

                return groupOperResult;
            }
        }
        BatchGroupResults results = new BatchGroupResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    @Override
    public Future<RpcResult<BatchGroupResults>> removeGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode) {
        return removeGroupsBatch(nodeYangId, normalNode, false);
    }

    public Future<RpcResult<BatchGroupResults>> removeGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                                   NormalizedNode normalNode, boolean isBarrierAfter) {

        Map.Entry<InstanceIdentifier, List<Group>> nodeGroupEntry = ofRpcUtils.getNodeGroupMap(nodeYangId,
                normalNode);

        if(nodeGroupEntry != null){
            InstanceIdentifier nodeIdent = nodeGroupEntry.getKey();

            if(preConfigurationCheck(nodeIdent)){
                RemoveGroupsBatchInputBuilder input = new RemoveGroupsBatchInputBuilder();
                input.setNode(new NodeRef(nodeIdent)).setBarrierAfter(isBarrierAfter);
                List<BatchRemoveGroups> batchRemoveGroupsList = new ArrayList<>();
                for(Group group : nodeGroupEntry.getValue()){
                    GroupId groupId = group.getGroupId();
                    BatchRemoveGroupsBuilder batchRemGroups = new BatchRemoveGroupsBuilder(group).setGroupId(groupId).
                            setGroupType(group.getGroupType()).setKey(new BatchRemoveGroupsKey(groupId));
                    batchRemoveGroupsList.add(batchRemGroups.build());
                }
                input.setBatchRemoveGroups(batchRemoveGroupsList);
                Future<RpcResult<RemoveGroupsBatchOutput>> future = delegateGroupBatchSvc.removeGroupsBatch(input.build());
                CompletableFuture<RpcResult<BatchGroupResults>> groupOperResult = new CompletableFuture<>();
                if(future instanceof  ListenableFuture){
                    Futures.addCallback(((ListenableFuture<RpcResult<RemoveGroupsBatchOutput>>) future), new
                            FutureCallback<RpcResult<RemoveGroupsBatchOutput>>() {
                                @Override
                                public void onSuccess(@Nullable RpcResult<RemoveGroupsBatchOutput> remGroupsBatchOutputRpcResult) {
                                    if(remGroupsBatchOutputRpcResult.isSuccessful()){
                                        groupOperResult.complete(RpcResultBuilder.success(BatchGroupResults.success()).build());
                                    }else {
                                        BatchGroupResults results = new BatchGroupResults(remGroupsBatchOutputRpcResult.getResult());
                                        RpcResultBuilder<BatchGroupResults> resultsRpcResult = RpcResultBuilder.failed();
                                        groupOperResult.complete(resultsRpcResult.withResult(results).build());
                                    }
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    BatchGroupResults results = new BatchGroupResults();
                                    results.setException(throwable);
                                    results.setErrorCode(550);
                                    RpcResultBuilder<BatchGroupResults> resultsRpcResult = RpcResultBuilder.failed();
                                    groupOperResult.complete(resultsRpcResult.withResult(results).build());
                                }
                            });
                }else {
                    RpcResultBuilder<BatchGroupResults> rpcResultBuilder = RpcResultBuilder.failed();
                    rpcResultBuilder.withRpcError(RpcResultBuilder.newError(
                            RpcError.ErrorType.RPC,"resultType-error", "Result is not listenable"));
                    groupOperResult.complete(rpcResultBuilder.build());
                }

                return groupOperResult;
            }
        }
        BatchGroupResults results = new BatchGroupResults();
        results.setErrorCode(550);
        return notMasterNodeRpcError(results);
    }

    @Override
    public void close() {
        ofNodeOwnerManager.close();
    }

    @Override
    public List<String> listLocalMasteredNodes() {
        return ofNodeOwnerManager.listLocalMasterNodes();
    }

    private boolean preConfigurationCheck(final InstanceIdentifier nodeId) {
        Preconditions.checkNotNull(nodeId, "FlowCapableNode identifier can not be null!");
        return ofNodeOwnerManager.isNodeActive(nodeId);
    }

    @Override
    public boolean isResourceLocal(Object resourceId) {
        LOG.info("OFFwdServiceImpl.isResourceLocal is called");
        Preconditions.checkArgument(resourceId instanceof InstanceIdentifier, "Target resources should provide" +
                "InstanceIdentifier Path");
        InstanceIdentifier<Node> nodeIdent = (InstanceIdentifier<Node>) resourceId;
        return ofNodeOwnerManager.isLocalMaster(nodeIdent);
    }

}
