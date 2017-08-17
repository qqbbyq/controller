/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.cache;

import com.google.gson.Gson;
import org.cmcc.aero.impl.utils.NodeNormalizedCodecUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by cmcc on 2017/8/16.
 */
public class NodeNormalizedCodecUtilsTest {

    private NodeNormalizedCodecUtils nodeNormalizedCodecUtils;
    private Flow flow;
    private InstanceIdentifier<Flow> flowPath;

    public NodeNormalizedCodecUtilsTest() throws Exception {
        ClassLoadingStrategy classLoadingStrategy = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        File yangExt = new File(getClass().getResource("/yang").toURI());
        File[] yangFiles = yangExt.listFiles();
        SchemaContext schemaContext = StmtTestUtils.parseYangSources(yangFiles);
        nodeNormalizedCodecUtils = new NodeNormalizedCodecUtils(schemaContext,
                classLoadingStrategy);
        nodeNormalizedCodecUtils.getCodec().onGlobalContextUpdated(schemaContext);
    }

    @Before
    public void setUp(){
        Long dpid = 28824221047116L;
        String nodeId = "openflow:"+dpid;
        int priority = 8192;
        short tableId = 5;
        String dst_ip = "100.100.100.100/32";
        long output_port = 121L;
        String flowId = OfTestUtils.getFlowId(tableId, dst_ip, output_port);
        flow = OfTestUtils.writeDstIpRule(dpid, tableId, priority, dst_ip, output_port);
        flowPath = OfTestUtils.createFlowPath(nodeId, tableId, flowId);
    }

    @Test
    public void genenrateJsonStringTest() throws IOException, URISyntaxException {
        String jsonString = nodeNormalizedCodecUtils.genenrateJsonString(flowPath, flow);
        System.out.println(String.format("FlowString is %s", jsonString));
    }

    @Test
    public void generateNormalizedNodeBytesTest() {
        byte[] flowBytes = nodeNormalizedCodecUtils.generateNormalizedNodeBytes(flowPath, flow);
        System.out.println(String.format("FlowBytes count %s",flowBytes.length));
    }

    @Test
    public void generateDataObjectTest() {
        byte[] flowBytes = nodeNormalizedCodecUtils.generateNormalizedNodeBytes(flowPath, flow);
        Map.Entry<InstanceIdentifier<?>, DataObject> flowMap = nodeNormalizedCodecUtils.generateDataObject(flowPath, flowBytes);
        Assert.assertTrue(flowMap.getValue() instanceof Flow);
        Flow deserializedFlow = (Flow)flowMap.getValue();
        Assert.assertEquals(flow.getKey(), deserializedFlow.getKey());
        Assert.assertEquals(flow.getTableId(), deserializedFlow.getTableId());
        Assert.assertTrue(deserializedFlow.getMatch().getLayer3Match() instanceof Ipv4Match);
        Assert.assertEquals(((Ipv4Match)flow.getMatch().getLayer3Match()).getIpv4Destination(),
                ((Ipv4Match)deserializedFlow.getMatch().getLayer3Match()).getIpv4Destination());
    }

    @Test
    public void obj2JsonTest(){
        Gson gson = new Gson();
        String flowJson = gson.toJson(new FlowBuilder(flow), FlowBuilder.class);
        System.out.println("Json String: " + flowJson);
    }
}
