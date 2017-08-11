/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RegisterTest {

  public static void main(String[] args) throws InterruptedException {
    GlobalRpcClient client = GlobalRpcClient.getInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    Thread.sleep(2000);
    System.out.println("Register test done.");
    System.exit(-1);
  }
}
