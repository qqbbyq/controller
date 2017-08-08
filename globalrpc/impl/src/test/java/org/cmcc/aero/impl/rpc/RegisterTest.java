package org.cmcc.aero.impl.rpc;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RegisterTest {

  public static void main(String[] args){
    GlobalRpcClient client = GlobalRpcClient.getInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    System.out.println("Register test done.");
  }
}
