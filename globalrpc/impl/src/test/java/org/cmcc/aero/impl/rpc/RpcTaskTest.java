/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;

import java.util.concurrent.ExecutionException;


/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RpcTaskTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    GlobalRpcClient client = GlobalRpcClient.getTmpInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    String str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL).get();

    java.util.concurrent.Future<GlobalRpcResult> r1 = client.globalCall("1", str, "printFuture");
    while (!r1.isDone()) {
      Thread.sleep(1000);
    }
    GlobalRpcResult r = r1.get();
    System.out.println("Rpc task test done with " + r );
    System.exit(-1);
  }
}
