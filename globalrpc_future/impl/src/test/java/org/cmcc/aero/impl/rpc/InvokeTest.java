package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.server.GlobalRpcUtils;
import org.cmcc.aero.impl.rpc.service.PrintService;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class InvokeTest {

  public static void main(String [] args){
    PrintService service = new PrintService();
//    String name = "zhuyuqing";
    Object [] paras = new Object[]{service};
//    GlobalRpcUtils.invoke(service, "print", null);
//    GlobalRpcUtils.invoke(service, "printName", paras);
    GlobalRpcUtils.invoke(service, "printService", paras);
  }
}
