package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.service.PrintService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class LocateTest {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    GlobalRpcClient client = GlobalRpcClient.getInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    Future<String> str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL);


    System.out.println("Locate test done with " + str.get() );
    System.exit(-1);
  }
}
