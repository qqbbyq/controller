package org.cmcc.aero.impl.rpc;

import akka.actor.*;
import org.cmcc.aero.impl.rpc.server.RpcManager;


/**
 * Created by zhuyuqing on 2017/8/7.
 */

public class StartAkkaSystem {
  public static void  main(String [] args) throws InterruptedException {

    ActorSystem system = ActorSystem.create("globalRpcSystem");
    ActorRef manager = system.actorOf(Props.create(RpcManager.class), "rpcManager");

    Thread.sleep(5000);
    System.exit(-1);

//    Config config = ConfigFactory.load();
//    Map<String, Object> configMap = new HashMap<>();
//    configMap.put("akka.remote.netty.tcp.host", "127.0.0.1");
//    configMap.put("akka.remote.netty.tcp.port", 5550);
//    Config customConf = ConfigFactory.parseMap(configMap).withFallback(config);


//    system.settings().config().getString("akka.remote.netty.tcp.host");
//    system.settings().config().getString("akka.remote.netty.tcp.host");



  }
}
