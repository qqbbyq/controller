package org.cmcc.aero.impl.rpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.server.RpcManager;
import org.cmcc.aero.impl.rpc.service.PrintService;
import org.cmcc.aero.impl.rpc.service.PrintService1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class ServiceStateTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
//
    GlobalRpcClient client = GlobalRpcClient.getInstance();
//
    client.register(new PrintService(), "PrintService", "PrintService");
    Thread.sleep(2000);

    java.util.concurrent.Future<String> str = client.locate("PrintService", "PrintService", 1000, GlobalRpcClient.Scale.CLUSTER);
    System.out.println("str:" + str.get());



    java.util.concurrent.Future<GlobalRpcResult> r1 = client.globalCall("1", str.get(), "printName", "1");
    java.util.concurrent.Future<GlobalRpcResult> r2 = client.globalCall("2", str.get(), "printName", "2");
    java.util.concurrent.Future<GlobalRpcResult> r3 = client.globalCall("3", str.get(), "printName", "3");
    java.util.concurrent.Future<GlobalRpcResult> r4 = client.globalCall("4", str.get(), "printName", "4");
    java.util.concurrent.Future<GlobalRpcResult> r5 = client.globalCall("5", str.get(), "printName", "5");
    java.util.concurrent.Future<GlobalRpcResult> r6 = client.globalCall("6", str.get(), "printName", "6");

    while(!r1.isDone() || !r2.isDone() || !r3.isDone() || !r4.isDone() || !r5.isDone() || !r6.isDone()){}
    System.out.println("Rpc task test done." );
    System.exit(-1);
  }
}
