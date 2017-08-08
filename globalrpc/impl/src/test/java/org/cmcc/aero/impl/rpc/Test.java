package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.server.GlobalRpcUtils;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class Test {

  public static void main(String [] args){
    PrintService service = new PrintService();
    String name = "zhuyuqing";
    Object [] paras = new Object[]{name};
    GlobalRpcUtils.invoke(service, "print", null);
    GlobalRpcUtils.invoke(service, "printName", paras);
  }
}
