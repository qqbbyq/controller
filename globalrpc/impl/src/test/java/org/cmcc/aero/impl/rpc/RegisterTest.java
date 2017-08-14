package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.service.PrintService;

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
