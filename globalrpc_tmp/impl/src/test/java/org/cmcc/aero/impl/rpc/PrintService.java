/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class PrintService implements GlobalRpcIntf {

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
    LOG.info("print {}", name);
  }

  public int print(){
    LOG.info("print");
    return 1027;
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


  public Future<String> printFuture(YangInstanceIdentifier instance, NormalizedNodeMessages.Node node) throws InterruptedException {
    ExecutorService es = Executors.newCachedThreadPool();
    return es.submit(new Task());
  }

}
