/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;

import org.cmcc.aero.impl.ofRpcs.OfRpcUtils;
import org.cmcc.aero.impl.ofRpcs.api.OFFwdService;
import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.impl.services.SalFlowsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalGroupsBatchServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalMetersBatchServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.FlowCapableTransactionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.input.update.grouping.UpdatedBatchedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.remove.flows.batch.input.BatchRemoveFlowsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.update.flows.batch.input.BatchUpdateFlowsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.SalGroupsBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.SalMetersBatchService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                      NormalizedNode normalNode) {

        Map.Entry<InstanceIdentifier, List> nodeFlowEntry = ofRpcUtils.getNodeFlowMap(nodeYangId, normalNode);
        if(nodeFlowEntry != null){
            InstanceIdentifier nodeIdent = nodeFlowEntry.getKey();
            if(preConfigurationCheck(nodeIdent)){
                List<Flow> targetFlows = (List<Flow>) nodeFlowEntry.getValue();
                RemoveFlowsBatchInputBuilder inputBuilder = new RemoveFlowsBatchInputBuilder().
                        setNode(new NodeRef(nodeIdent)).setBarrierAfter(false);
                List<BatchRemoveFlows> batchRemoveFlows = new ArrayList<>();
                for(Flow flow : targetFlows){
                    BatchRemoveFlows batchRemoveFlow = new BatchRemoveFlowsBuilder(flow).setFlowId(flow.getId()).
                            setKey(new BatchRemoveFlowsKey(flow.getId())).setTableId(flow.getTableId()).build();
                    batchRemoveFlows.add(batchRemoveFlow);
                }
                inputBuilder.setBatchRemoveFlows(batchRemoveFlows);
                return delegateFlowBatchSvc.removeFlowsBatch(inputBuilder.build());
            }
        }
        return notMasterNodeRpcError();
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

    public Future<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                      NormalizedNode normalNode) {
        Map.Entry<InstanceIdentifier, List> nodeFlowEntry = ofRpcUtils.getNodeFlowMap(nodeYangId, normalNode);
        if(nodeFlowEntry != null){
            InstanceIdentifier nodeIdent = nodeFlowEntry.getKey();
            if(preConfigurationCheck(nodeIdent)){
                UpdateFlowsBatchInputBuilder inputBuilder = new UpdateFlowsBatchInputBuilder().
                        setNode(new NodeRef(nodeIdent)).setBarrierAfter(false);
                List<Flow> targetFlows = (List<Flow>) nodeFlowEntry.getValue();
                List<BatchUpdateFlows> batchUpdateFlows = new ArrayList<>();
                for(Flow flow : targetFlows){
                    BatchUpdateFlowsBuilder batchUpdateFlow = new BatchUpdateFlowsBuilder().setFlowId(flow.getId()).
                            setKey(new BatchUpdateFlowsKey(flow.getId())).
                            setUpdatedBatchedFlow(new UpdatedBatchedFlowBuilder(flow).build());
                    batchUpdateFlows.add(batchUpdateFlow.build());
                }
                inputBuilder.setBatchUpdateFlows(batchUpdateFlows);
                return delegateFlowBatchSvc.updateFlowsBatch(inputBuilder.build());
            }
        }
        return notMasterNodeRpcError();
    }

    @Override
    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                NormalizedNode normalNode) {
        Map.Entry<InstanceIdentifier, List> nodeFlowEntry = ofRpcUtils.getNodeFlowMap(nodeYangId, normalNode);

        if(nodeFlowEntry != null){
            InstanceIdentifier nodeIdent = nodeFlowEntry.getKey();
            if(preConfigurationCheck(nodeIdent)){
                AddFlowsBatchInputBuilder inputBuilder = new AddFlowsBatchInputBuilder().
                        setNode(new NodeRef(nodeIdent)).setBarrierAfter(false);
                List<BatchAddFlows> batchAddFlows = new ArrayList<>();
                List<Flow> targetFlows = (List<Flow>) nodeFlowEntry.getValue();
                for(Flow flow : targetFlows){
                    BatchAddFlows batchAddFlow = new BatchAddFlowsBuilder(flow).setFlowId(flow.getId()).
                            setKey(new BatchAddFlowsKey(flow.getId())).setTableId(flow.getTableId()).build();
                    batchAddFlows.add(batchAddFlow);
                }
                inputBuilder.setBatchAddFlows(batchAddFlows);
                return delegateFlowBatchSvc.addFlowsBatch(inputBuilder.build());
            }
        }
        return notMasterNodeRpcError();
    }

    @Override
    public Future<RpcResult<RemoveMetersBatchOutput>> removeMetersBatch(RemoveMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            return delegateMeterBatchSvc.removeMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public Future<RpcResult<UpdateMetersBatchOutput>> updateMetersBatch(UpdateMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            return delegateMeterBatchSvc.updateMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public Future<RpcResult<AddMetersBatchOutput>> addMetersBatch(AddMetersBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            return delegateMeterBatchSvc.addMetersBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public Future<RpcResult<AddGroupsBatchOutput>> addGroupsBatch(AddGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            return delegateGroupBatchSvc.addGroupsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public Future<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBatch(RemoveGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
            return delegateGroupBatchSvc.removeGroupsBatch(input);
        }else {
            return notMasterNodeRpcError();
        }
    }

    @Override
    public Future<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBatch(UpdateGroupsBatchInput input) {
        InstanceIdentifier nodeIdent = input.getNode().getValue();
        if(preConfigurationCheck(nodeIdent)){
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
