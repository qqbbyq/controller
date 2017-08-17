/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.controller.cluster.datastore.node.utils.serialization.NormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;

import java.util.concurrent.Future;

import java.util.concurrent.ExecutionException;


/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RpcTaskTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    GlobalRpcClient client = GlobalRpcClient.getTmpInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    String str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL);
    System.out.println(str);
    NormalizedNode<?, ?> normalizedNode = ImmutableContainerNodeBuilder.create().withNodeIdentifier(
      new YangInstanceIdentifier.NodeIdentifier(
        QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test")
      )).withChild(ImmutableNodes.leafNode(
      QName.create(
        QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test"), "desc")
        , "foo")).build();
    java.util.concurrent.Future<GlobalRpcResult> r1 = client.globalCall(
      "1", str, "printFuture", YangInstanceIdentifier.EMPTY,
      NormalizedNodeSerializer.serialize(normalizedNode));
    while (!r1.isDone()) {
      Thread.sleep(1000);
    }
    GlobalRpcResult r = r1.get();
    System.out.println("Rpc task test done with " + r );
    System.exit(-1);
  }
}
