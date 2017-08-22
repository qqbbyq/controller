/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import java.util.concurrent.ExecutionException;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class LocateTest {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    GlobalRpcClient client = GlobalRpcClient.getTmpInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    String str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL).get();


    System.out.println("Locate test done with " + str );
    System.exit(-1);
  }
}
