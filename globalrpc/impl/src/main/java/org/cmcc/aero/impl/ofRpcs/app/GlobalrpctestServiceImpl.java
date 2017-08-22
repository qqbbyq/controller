/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.app;

import com.google.common.collect.Lists;
import org.cmcc.aero.impl.ofRpcs.api.GlobalFlowRpcService;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.yang.gen.v1.urn.aero.yang.globalrpctest.rev170801.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.add.flows.batch.input.BatchAddFlowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by cmcc on 2017/8/10.
 */
public class GlobalrpctestServiceImpl implements GlobalrpctestService {
    private static final String OPENFLOW_PREFIX = "openflow:";
    private Logger LOG = LoggerFactory.getLogger(GlobalrpctestServiceImpl.class);

    private Random random;
    private GlobalFlowRpcService flowRpcService;

    public GlobalrpctestServiceImpl(GlobalFlowRpcService flowRpcService){
        this.flowRpcService = flowRpcService;
        this.random = new Random(System.currentTimeMillis());
    }

    public GlobalrpctestServiceImpl(){
        this.random = new Random(System.currentTimeMillis());
    }

    public void setFlowRpcService(GlobalFlowRpcService flowRpcService){
        this.flowRpcService = flowRpcService;
    }

    @Override
    public Future<RpcResult<GetLocalOfnodesOutput>> getLocalOfnodes() {
        List<String> nodeIdList = flowRpcService.listLocalMasteredNodes();
        GetLocalOfnodesOutputBuilder outputBuilder = new GetLocalOfnodesOutputBuilder();
        outputBuilder.setOfnodes(nodeIdList);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<BulkWriteFlowsOutput>> bulkWriteFlows(BulkWriteFlowsInput input) {
        String nodeId = input.getNodeId();
        Integer num = input.getNumber();
        String flowStr = bulkLaunchFlowEntries(nodeId, num);
        BulkWriteFlowsOutputBuilder outputBuilder = new BulkWriteFlowsOutputBuilder();
        outputBuilder.setFlow(flowStr);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<WriteFlowsOutput>> writeFlows(WriteFlowsInput input) {
        String nodeId = input.getNodeId();
        String flowStr = launchFlowEntries(nodeId);
        WriteFlowsOutputBuilder outputBuilder = new WriteFlowsOutputBuilder();
        outputBuilder.setFlow(flowStr);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<GetFlowOutput>> getFlow(GetFlowInput input) {
        String nodeId = input.getNodeId();
        String flowId = input.getFlowId();
        String flowStr = flowRpcService.getFlowString(nodeId, flowId);
        GetFlowOutputBuilder outputBuilder = new GetFlowOutputBuilder();
        outputBuilder.setFlowStr(flowStr);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    private short generateRandomTableId() {
        return (short)random.nextInt(200);
    }

    private int generateRandomOfPort(){
        return random.nextInt(65500) & 0xFFFFFFFF;
    }

    private String generateRandomIPStr(){
        byte[] ipBytes = new byte[2];
        random.nextBytes(ipBytes);
        return getIPStr("192.168.", ipBytes);
    }
    private String getIPStr(String basicStr, byte[] ipBytes){
        String ipStr = basicStr;
        for(int i =0; i<ipBytes.length-1;i++){
            ipStr = ipStr.concat(String.valueOf((int)ipBytes[i] & 0xFF)).concat(".");
        }
        ipStr = ipStr.concat(String.valueOf(ipBytes[ipBytes.length - 1] &0xFF));
        return ipStr;
    }

    private int generateRandomPriority() {
        return (random.nextInt(65536) & 0xFFFFFFFF);
    }

    private String launchFlowEntries(String ofNodeID){
        LOG.info("Launch openflows on node {}.", ofNodeID);

        long startGenData = System.nanoTime();
        Long dpid = parseDPID(ofNodeID);
        short tableId = generateRandomTableId();
        String basicIpStr = generateRandomIPStr();
        String dstIp = basicIpStr + "/32";
        int outPort = generateRandomOfPort();
        int priority = generateRandomPriority();
        Flow flow = generateDstIpRule(dpid, tableId, priority, dstIp, outPort);
        NodeBuilder nodeBuilder = createNodeBuilder(dpid);

        long afterGenData = System.nanoTime();
        long genDataCost = TimeUnit.NANOSECONDS.toMicros(afterGenData - startGenData);

        LOG.info("Generate openflow cfg: tableID-{}, IP_Start-{},priority-{},output:port-{}, dpids-{}", tableId,
                basicIpStr, priority, outPort, dpid);

        long beforeWrite = System.nanoTime();
        writeFlowDirect(Lists.newArrayList(flow), nodeBuilder);

        long afterWrite = System.nanoTime();
        long writeDataCost = TimeUnit.NANOSECONDS.toMicros(afterWrite - beforeWrite);

        return String.format("%s generate openflow data cost %d us, write openflow cost %d us",
                this.getClass().getSimpleName(), genDataCost, writeDataCost);
    }

    private String bulkLaunchFlowEntries(String ofNodeID, Integer number){
        LOG.info("bulk Launch openflows on node {}.", ofNodeID);
        long startGenData = System.nanoTime();

        short[] tableIds = new short[number];
        String[] dstIps = new String[number];
        int[] outPorts = new int[number];
        int[] priorities = new int[number];

        Long dpid = parseDPID(ofNodeID);
        String basicIpStr;

        for (int i = 0; i < number; ++i ) {
            tableIds[i] = generateRandomTableId();
            basicIpStr = generateRandomIPStr();
            dstIps[i] = generateRandomIPStr() + "/32";
            outPorts[i] = generateRandomOfPort();
            priorities[i] = generateRandomPriority();
            LOG.info("bulk Generate openflow cfg: tableID-{}, IP_Start-{},priority-{},output:port-{}, dpids-{}", tableIds[i],
              basicIpStr, priorities[i], outPorts[i], dpid);

        }
        List<Flow> flows = new ArrayList<>();
        NodeBuilder nodeBuilder = createNodeBuilder(dpid);

        for(int i = 0; i < number; ++i ) {
            flows.add(generateDstIpRule(dpid, tableIds[i], priorities[i], dstIps[i], outPorts[i]));
        }

        long afterGenData = System.nanoTime();
        long genDataCost = TimeUnit.NANOSECONDS.toMicros(afterGenData - startGenData);
        long beforeWrite = System.nanoTime();

        writeFlowDirect(flows, nodeBuilder);

        long afterWrite = System.nanoTime();
        long writeDataCost = TimeUnit.NANOSECONDS.toMicros(afterWrite - beforeWrite);

        return String.format("%s bulk generate openflow data size %d,  cost %d us, bulk write openflow cost %d us",
          this.getClass().getSimpleName(), number, genDataCost, writeDataCost);
    }

    private Long parseDPID(String ofNodeID) {
        if(ofNodeID.startsWith(OPENFLOW_PREFIX)){
            return Long.parseLong(ofNodeID.substring(OPENFLOW_PREFIX.length()));
        }
        return Long.parseLong(ofNodeID);
    }

    private String getNodeName(long dpidLong) {
        return OPENFLOW_PREFIX + dpidLong;
    }

    private NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    private NodeBuilder createNodeBuilder(long dpidLong) {
        return createNodeBuilder(getNodeName(dpidLong));
    }

    private InstanceIdentifier<Flow> createFlowPath(FlowBuilder flowBuilder, NodeBuilder nodeBuilder) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey()).build();
    }

    private FlowBuilder initFlowBuilder(FlowBuilder flowBuilder, String flowName, short table) {
        final FlowId flowId = new FlowId(flowName);
        flowBuilder
                .setId(flowId)
                .setStrict(true)
                .setBarrier(false)
                .setTableId(table)
                .setKey(new FlowKey(flowId))
                .setFlowName(flowName)
                .setHardTimeout(0)
                .setIdleTimeout(0);
        return flowBuilder;
    }

    private MatchBuilder createDstL3IPv4Match(MatchBuilder matchBuilder, Ipv4Prefix dstip) {

        EthernetMatchBuilder eth = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(0x0800L));
        eth.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(eth.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Destination(dstip);

        matchBuilder.setLayer3Match(ipv4match.build());

        return matchBuilder;
    }

    private InstructionBuilder createOutputPortInstructions(InstructionBuilder ib, Long dpidLong, Long port) {

        NodeConnectorId ncid = new NodeConnectorId("openflow:" + dpidLong + ":" + port);
        LOG.debug("createOutputPortInstructions() Node Connector ID is - Type=openflow: DPID={} inPort={} ",
                dpidLong, port);

        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder oab = new OutputActionBuilder();
        oab.setOutputNodeConnector(ncid);

        ab.setAction(new OutputActionCaseBuilder().setOutputAction(oab.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // Create an Apply Action
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());

        return ib;
    }

    public Flow generateDstIpRule(Long dpidLong, short tableId, int priority, String dstIp, long outPort) {
        FlowBuilder flowBuilder = new FlowBuilder();
        String flowName = "TableID_" + tableId + "_DstIP_" + dstIp + "_Output_" + outPort;
        initFlowBuilder(flowBuilder, flowName, tableId).setPriority(priority);
        MatchBuilder matchBuilder = new MatchBuilder();
        createDstL3IPv4Match(matchBuilder, new Ipv4Prefix(dstIp));
        flowBuilder.setMatch(matchBuilder.build());

        // Create the OF Actions and Instructions
        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();

        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();

        // Call the InstructionBuilder Methods Containing Actions
        createOutputPortInstructions(ib, dpidLong, outPort);
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());
        return flowBuilder.build();
    }

   /*public void writeDstIpRule(Long dpidLong, short tableId, int priority, String dstIp, long outPort) {
        FlowBuilder flowBuilder = new FlowBuilder();
        NodeBuilder nodeBuilder = createNodeBuilder(dpidLong);

        String flowName = "TableID_" + tableId + "_DstIP_" + dstIp + "_Output_" + outPort;
        initFlowBuilder(flowBuilder, flowName, tableId).setPriority(priority);
        MatchBuilder matchBuilder = new MatchBuilder();
        createDstL3IPv4Match(matchBuilder, new Ipv4Prefix(dstIp));
        flowBuilder.setMatch(matchBuilder.build());

        // Create the OF Actions and Instructions
        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();

        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();

        // Call the InstructionBuilder Methods Containing Actions
        createOutputPortInstructions(ib, dpidLong, outPort);
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Add InstructionBuilder to the Instruction(s)Builder List
        isb.setInstruction(instructions);

        // Add InstructionsBuilder to FlowBuilder
        flowBuilder.setInstructions(isb.build());

        writeFlowDirect(Lists.newArrayList(flowBuilder.build()), nodeBuilder);
    }*/

    private InstanceIdentifier<Flow> createFlowIid(Flow flow, InstanceIdentifier<Node> nodeIid) {
        return nodeIid.builder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId()))
                .child(Flow.class, new FlowKey(flow.getId()))
                .build();
    }

    private void writeFlowDirect(final List<Flow> flows, final NodeBuilder nodeBuilder){
        LOG.debug("writeFlowDirect is called");
        LOG.debug("writeFlow on MasterNode");
        Future<GlobalRpcResult> r = this.flowRpcService.addFlowsBatch(nodeBuilder.getId().getValue(), flows);

        try {
            LOG.info("writeFlow res={}", r.get());
        } catch (Exception e) {
            LOG.error("writeFlow error", e.getMessage());
            e.printStackTrace();
        }

    }
}
