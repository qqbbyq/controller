/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.cache;

import com.google.common.collect.Lists;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by cmcc on 2017/8/16.
 */
public class OfTestUtils {

    private static Logger LOG = LoggerFactory.getLogger(OfTestUtils.class);
    private static Random random = new Random();
    private static final String OPENFLOW_PREFIX = "openflow:";

    private short generateRandomTableId() {
        return (short)random.nextInt(200);
    }

    private int generateRandomOfPort(){
        return random.nextInt(65500) & 0xFFFFFFFF;
    }

    private static String generateRandomIPStr(){
        byte[] ipBytes = new byte[2];
        random.nextBytes(ipBytes);
        return getIPStr("192.168.", ipBytes);
    }
    private static String getIPStr(String basicStr, byte[] ipBytes){
        String ipStr = basicStr;
        for(int i =0; i<ipBytes.length-1;i++){
            ipStr = ipStr.concat(String.valueOf((int)ipBytes[i] & 0xFF)).concat(".");
        }
        ipStr = ipStr.concat(String.valueOf(ipBytes[ipBytes.length - 1] &0xFF));
        return ipStr;
    }

    private static int generateRandomPriority() {
        return (random.nextInt(65536) & 0xFFFFFFFF);
    }

    private static Long parseDPID(String ofNodeID) {
        if(ofNodeID.startsWith(OPENFLOW_PREFIX)){
            return Long.parseLong(ofNodeID.substring(OPENFLOW_PREFIX.length()));
        }
        return Long.parseLong(ofNodeID);
    }

    private static String getNodeName(long dpidLong) {
        return OPENFLOW_PREFIX + dpidLong;
    }

    private static NodeBuilder createNodeBuilder(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder;
    }

    public static NodeBuilder createNodeBuilder(long dpidLong) {
        return createNodeBuilder(getNodeName(dpidLong));
    }

    public static InstanceIdentifier<Flow> createFlowPath(String nodeId, short tableId, String flowId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeId)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(Flow.class, new FlowKey(new FlowId(flowId))).build();
    }

    private static FlowBuilder initFlowBuilder(FlowBuilder flowBuilder, String flowName, short table) {
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

    private static MatchBuilder createDstL3IPv4Match(MatchBuilder matchBuilder, Ipv4Prefix dstip) {

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

    private static InstructionBuilder createOutputPortInstructions(InstructionBuilder ib, Long dpidLong, Long port) {

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

    public static String getFlowId(short tableId, String dstIp, long outPort){
        return"TableID_" + tableId + "_DstIP_" + dstIp + "_Output_" + outPort;
    }

    public static Flow writeDstIpRule(Long dpidLong, short tableId, int priority, String dstIp, long outPort) {
        FlowBuilder flowBuilder = new FlowBuilder();
        String flowName = getFlowId(tableId, dstIp, outPort);
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

    public static InstanceIdentifier<Flow> createFlowIid(Flow flow, InstanceIdentifier<Node> nodeIid) {
        return nodeIid.builder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId()))
                .child(Flow.class, new FlowKey(flow.getId()))
                .build();
    }
}
