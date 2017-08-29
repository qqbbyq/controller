/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.server.GlobalRpcUtils;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class InvokeTest {

  public static void main(String [] args){

    PrintService service = new PrintService();
    String name = "zhuyuqing";
    Object [] paras = new Object[]{name};
    GlobalRpcUtils.invoke(service, "print", null);
    GlobalRpcUtils.invoke(service, "printName", paras);
  }
}
