package org.cmcc.aero.impl.rpc.service;

import org.cmcc.aero.impl.rpc.GlobalRpcIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class PrintService3 implements GlobalRpcIntf {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  private String serviceName;
  private String serviceType;

  @Override
  public boolean isResourceLocal(Object resourceId){
    return true;
  }

  @Override
  public String getServiceName() {
    return this.serviceName;
  }

  @Override
  public String getServiceType() {
    return this.serviceType;
  }


  public void printName(String name){
    LOG.info("print3 {}", name);
  }

  public int print(){
    LOG.info("print3");
    return 1030;
  }

  public static class Task implements Callable<String> {
    @Override
    public String call() throws Exception {
      Thread.sleep(5000);
      String tid = String.valueOf(Thread.currentThread().getId());
      System.out.printf("Thread#%s : in call\n", tid);
      return tid;
    }
  }


  public Future<String> printFuture() throws InterruptedException {
    ExecutorService es = Executors.newCachedThreadPool();
    return es.submit(new Task());
  }

}
