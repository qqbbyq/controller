/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.impl;

import org.cmcc.aero.impl.ofRpcs.api.GlobalFlowRpcService;
import org.cmcc.aero.impl.rpc.GlobalRpcClient;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/8/8.
 */
public class GlobalFlowRpcServiceImpl implements GlobalFlowRpcService {
    private OFFwdServiceImpl delegateSvc;
    private GlobalRpcClient globalRpcClient;
    private AtomicLong txLong = new AtomicLong(0);

    public GlobalFlowRpcServiceImpl(OFFwdServiceImpl openflowFwdingSvc, GlobalRpcClient globalRpcClient){
        this.delegateSvc =openflowFwdingSvc;
        this.globalRpcClient = globalRpcClient;
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

    public Future<GlobalRpcResult> removeFlowsBatch(RemoveFlowsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "removeFlowsBatch", input);
    }

    private synchronized String generateTxId() {
        return String.valueOf(this.getClass().getSimpleName()) + "-" + txLong.incrementAndGet();
    }

    public Future<GlobalRpcResult> updateFlowsBatch(UpdateFlowsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "updateFlowsBatch", input);
    }

    public Future<GlobalRpcResult> addFlowsBatch(AddFlowsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "addFlowsBatch", input);
    }

    public Future<GlobalRpcResult> removeMetersBatch(RemoveMetersBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "removeMetersBatch", input);
    }

    public Future<GlobalRpcResult> updateMetersBatch(UpdateMetersBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "updateMetersBatch", input);
    }

    public Future<GlobalRpcResult>  addMetersBatch(AddMetersBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "addMetersBatch", input);
    }

    public Future<GlobalRpcResult> addGroupsBatch(AddGroupsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "addGroupsBatch", input);
    }

    public Future<GlobalRpcResult>  removeGroupsBatch(RemoveGroupsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "removeGroupsBatch", input);
    }

    public Future<GlobalRpcResult>  updateGroupsBatch(UpdateGroupsBatchInput input) {
        InstanceIdentifier nodeIid = input.getNode().getValue();
        String resourecePath = locateOFNode(nodeIid);
        String transactionId = generateTxId();
        return globalRpcClient.globalCall(transactionId, resourecePath, "updateGroupsBatch", input);
    }

    @Override
    public void close() {
        this.delegateSvc.close();
    }

    @Override
    public List<String> listLocalMasteredNodes() {
        return delegateSvc.listLocalMasteredNodes();
    }
}
