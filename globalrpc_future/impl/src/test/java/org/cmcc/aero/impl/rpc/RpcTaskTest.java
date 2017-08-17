package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.service.PrintService;

import java.util.concurrent.Future;

import java.util.concurrent.ExecutionException;


/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RpcTaskTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    GlobalRpcClient client = GlobalRpcClient.getTmpInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    Future<String> str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL);

    java.util.concurrent.Future<GlobalRpcResult> r1 = client.globalCall("1", str.get(), "printFuture");
    GlobalRpcResult r = r1.get();
    System.out.println("Rpc task test done with " + r.toString() );
    System.exit(-1);
  }
}
