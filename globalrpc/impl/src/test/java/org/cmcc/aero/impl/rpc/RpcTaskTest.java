package org.cmcc.aero.impl.rpc;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import scala.concurrent.Future;


/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class RpcTaskTest {

  public static void main(String[] args) throws InterruptedException {
    GlobalRpcClient client = GlobalRpcClient.getInstance();
    client.register(new PrintService(), "PrintService", "PrintService");
    String str = client.locate("PrintService", "PrintService", 1, GlobalRpcClient.Scale.LOCAL);

    Future<GlobalRpcResult> r1 = client.globalCall("1", str, "print");
    while (!r1.isCompleted()) {
      Thread.sleep(1000);
    }
    GlobalRpcResult r = r1.value().get().get();
    System.out.println("Rpc task test done with " + r );
  }
}
