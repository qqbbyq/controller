package org.cmcc.aero.impl.rpc;

/**
 * Created by zhuyuqing on 2017/8/3.
 */

public interface GlobalRpcIntf {

  // true, local find target resource; false, target not local
  public boolean isResourceLocal(Object resourceId);


}
