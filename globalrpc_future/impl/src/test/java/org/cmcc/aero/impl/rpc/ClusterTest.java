package org.cmcc.aero.impl.rpc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.cmcc.aero.impl.rpc.server.RpcManager;
import org.cmcc.aero.impl.rpc.service.PrintService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhuyuqing on 2017/8/8.
 */

public class ClusterTest {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    Config config = ConfigFactory.load();
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("akka.remote.netty.tcp.host", "127.0.0.1");
    configMap.put("akka.remote.netty.tcp.port", 5551);
    Config customConf = ConfigFactory.parseMap(configMap).withFallback(config);


    ActorSystem system = ActorSystem.create("globalRpcSystem", customConf);
    ActorRef manager = system.actorOf(Props.create(RpcManager.class), "rpcManager");



//
    GlobalRpcClient client = GlobalRpcClient.getTmpInstance();
//
    client.register(new PrintService(), "PrintService", "PrintService");

    Thread.sleep(2000);
    client.updateRpcManager(manager, system);
    java.util.concurrent.Future<String> str = client.locate("PrintService", "PrintService", 1000, GlobalRpcClient.Scale.CLUSTER);
    System.out.println("str:" + str);

    java.util.concurrent.Future<GlobalRpcResult> r1 = client.globalCall("1", str.get(), "print");
    GlobalRpcResult r = r1.get();
    System.out.println("Rpc task test done with " + r );
    System.exit(-1);
  }
}
