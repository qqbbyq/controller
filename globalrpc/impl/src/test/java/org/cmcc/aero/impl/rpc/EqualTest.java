package org.cmcc.aero.impl.rpc;

import akka.pattern.PatternsCS;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.cmcc.aero.impl.rpc.message.LocateService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhuyuqing on 2017/8/24.
 */

public class EqualTest {


  private static LoadingCache<LocateService, String> locateCache = CacheBuilder.newBuilder()//
//    .refreshAfterWrite(30, TimeUnit.SECONDS)// 给定时间内没有被读/写访问，则回收。
    .expireAfterWrite(30, TimeUnit.SECONDS)//给定时间内没有写访问，则回收。
    // .expireAfterAccess(3, TimeUnit.SECONDS)// 缓存过期时间为3秒
    .maximumSize(100).// 设置缓存个数
    build(new CacheLoader<LocateService, String>() {

    public String load(LocateService key) throws ExecutionException {
      System.out.println(key + " load in cache");

      return "done";
    }
  });


  public static void main(String[] args) throws ExecutionException {
    InstanceIdentifier<Node> nodeIid = InstanceIdentifier.create(Nodes.class).
      child(Node.class, new NodeKey(new NodeId("123")));

    InstanceIdentifier<Node> nodeIid1 = InstanceIdentifier.create(Nodes.class).
      child(Node.class, new NodeKey(new NodeId("123")));
    System.out.println(nodeIid.equals(nodeIid1));

    LocateService l = new LocateService("1", "2", nodeIid, GlobalRpcClient.Scale.CLUSTER);
    LocateService l1 = new LocateService("1", "2", nodeIid1, GlobalRpcClient.Scale.CLUSTER);
    System.out.println(l.equals(l1));
    System.out.println(locateCache.get(l));
    System.out.println(locateCache.get(l1));
  }
}
