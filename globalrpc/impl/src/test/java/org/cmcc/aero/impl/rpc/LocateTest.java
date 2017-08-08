package org.cmcc.aero.impl.rpc;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class LocateTest {

  public static void main(String[] args){
    GlobalRpcClient client = GlobalRpcClient.getInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    String str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL);


    System.out.println("Locate test done with " + str );
  }
}
