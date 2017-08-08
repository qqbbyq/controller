package org.cmcc.aero.impl.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuyuqing on 2017/8/4.
 */

public class PrintService implements GlobalRpcIntf {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Override
  public boolean isResourceLocal(Object resourceId){
    return true;
  }


  public void printName(String name){
    LOG.info("print {}", name);
  }

  public int print(){
    LOG.info("print");
    return 1027;
  }

}
