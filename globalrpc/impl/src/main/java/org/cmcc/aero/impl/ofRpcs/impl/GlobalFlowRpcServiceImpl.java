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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
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

    private CompletableFuture<String> locateOFNode(InstanceIdentifier<Node> nodeIdent){
        return  (CompletableFuture<String>) globalRpcClient.locate(delegateSvc.getServiceName(), delegateSvc.getServiceName(), nodeIdent,
                GlobalRpcClient.Scale.CLUSTER);
    }

    public Future<GlobalRpcResult> removeFlowsBatch(String nodeId, List<Flow> flows) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeFlowNodeNormal(nodeId, flows);
        if(normalizedNode!=null){

            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            String transactionId = generateTxId();

            CompletableFuture<GlobalRpcResult> completableFuture = locateOFNode(nodeIid)
              .thenCompose(path ->
                (CompletableFuture <GlobalRpcResult>)globalRpcClient.globalCall(transactionId, path,
              "removeFlowsBatch", normalizedNode.getKey(), normalizedNode.getValue())
              );

            /*
            Future<String> resourecePath = locateOFNode(nodeIid);

            CompletableFuture<GlobalRpcResult> completableFuture = new CompletableFuture();
            ((CompletableFuture)resourecePath).thenApply(new Consumer<String>() {
                @Override
                public void accept(String resourecePath1) {

                    Future<GlobalRpcResult> future = globalRpcClient.globalCall(transactionId, resourecePath1,
                      "removeFlowsBatch", normalizedNode.getKey(), normalizedNode.getValue());
                    ((CompletableFuture)future).thenAccept(rpcResult -> completableFuture.complete(rpcResult));

                }
            });*/
            removeFlowsFromCache(nodeIid, flows);
            return completableFuture;
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

    public Future<GlobalRpcResult> addFlowsBatch(String nodeId, List<Flow> flows) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeFlowNodeNormal(nodeId, flows);
        if(normalizedNode!= null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            addFlows2Cache(nodeIid, flows);
            String transactionId = generateTxId();

            // Only for test, not use globalcall
//            delegateSvc.addFlowsBatch(normalizedNode.getKey(), normalizedNode.getValue());
            return locateOFNode(nodeIid).thenCompose(resourcePath ->
              (CompletableFuture<GlobalRpcResult>)
                globalRpcClient.globalCall(transactionId, resourcePath, "addFlowsBatch", normalizedNode.getKey(),
                normalizedNode.getValue())
            );
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

    public Future<GlobalRpcResult> removeMetersBatch(String nodeId, List<Meter> meters) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeMeterNodeNormal(nodeId,
                meters);
        if(normalizedNode!=null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            String transactionId = generateTxId();

            removeMetersFromCache(nodeIid, meters);

            return locateOFNode(nodeIid).thenCompose(path ->
              (CompletableFuture<GlobalRpcResult>) globalRpcClient.globalCall(transactionId, (String)path,
                "removeMetersBatch", normalizedNode.getKey(), normalizedNode.getValue())
            );
        } else {
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void removeMetersFromCache(InstanceIdentifier targetNode, List<Meter> meters) {
        List<Long> removedMeterIds = Lists.newArrayList();
        for(Meter batchMeter : meters){
            Long meterId = batchMeter.getMeterId().getValue();
            removedMeterIds.add(meterId);
        }
        cacheService.batchRemoveMeters(targetNode, removedMeterIds);
    }

    public Future<GlobalRpcResult>  addMetersBatch(String nodeId, List<Meter> meters) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeMeterNodeNormal(nodeId,
                meters);
        if(normalizedNode !=null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            addMeters2Cache(nodeIid, meters);
            String transactionId = generateTxId();
            Future<GlobalRpcResult> completableFuture = locateOFNode(nodeIid).thenCompose(resourcePath ->
              (CompletableFuture<GlobalRpcResult>) globalRpcClient.globalCall(transactionId, (String) resourcePath, "addMetersBatch",
                normalizedNode.getKey(), normalizedNode.getValue())
            );
            return  completableFuture;
        }else {
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void addMeters2Cache(InstanceIdentifier nodeIdent, List<Meter> meters) {
        for(Meter meter : meters){
            Long meterId = meter.getMeterId().getValue();
            cacheService.addMeter(nodeIdent, meterId, meter);
        }
    }

    public Future<GlobalRpcResult> addGroupsBatch(String nodeId, List<Group> groups) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeGroupNodeNormal(nodeId,
                groups);
        if(normalizedNode!=null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            addGroups2Cache(nodeIid, groups);
            String transactionId = generateTxId();

            return locateOFNode(nodeIid).thenCompose(resourcePath ->
              (CompletableFuture<GlobalRpcResult>)globalRpcClient.globalCall(transactionId, (String) resourcePath, "addGroupsBatch",
                normalizedNode.getKey(), normalizedNode.getValue())
            );
        } else {
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void addGroups2Cache(InstanceIdentifier node, List<Group> groups) {
        for(Group batchGroups : groups ){
            Long groupId = batchGroups.getGroupId().getValue();
            cacheService.addGroup(node, groupId, batchGroups);
        }
    }

    public Future<GlobalRpcResult> removeGroupsBatch(String nodeId, List<Group> groups) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?,?>> normalizedNode = ofRpcUtils.makeGroupNodeNormal(nodeId,
                groups);
        if(normalizedNode!=null){
            InstanceIdentifier<Node> nodeIid = ofRpcUtils.createNodeId(nodeId);
            String transactionId = generateTxId();

            CompletableFuture<GlobalRpcResult> future = locateOFNode(nodeIid).thenCompose(resourcePath ->
              (CompletableFuture<GlobalRpcResult>)globalRpcClient.globalCall(transactionId, resourcePath,
              "removeGroupsBatch", normalizedNode.getKey(), normalizedNode.getValue())
            );
            removeGroupsFromCache(nodeIid, groups);

            return future;
        }else {
            LOG.error("Error wile make node and flows normalized");
            return CompletableFuture.completedFuture(GlobalRpcResult.failure(503,
                    "Error wile make node and flows normalized"));
        }
    }

    private void removeGroupsFromCache(InstanceIdentifier targetNode, List<Group> groups) {
        List<Long> removedGroupIds = Lists.newArrayList();
        for(Group group : groups){
            removedGroupIds.add(group.getGroupId().getValue());
        }
        cacheService.batchRemovceGroups(targetNode, removedGroupIds);
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
