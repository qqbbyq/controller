/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.rpc;

import java.io.Serializable;

/**
 * Created by zhuyuqing on 2017/8/3.
 */

public interface GlobalRpcIntf extends Serializable{

  // true, local find target resource; false, target not local
  public boolean isResourceLocal(Object resourceId);

  public String getServiceName();

  public String getServiceType();

}
