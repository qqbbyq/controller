/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.app;

import org.cmcc.aero.impl.ofRpcs.api.GlobalFlowRpcService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.aero.yang.globalrpctest.rev170801.GlobalrpctestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cmcc on 2017/8/10.
 */
public class GlobalFlowRpcSvcApp {
    private Logger LOG = LoggerFactory.getLogger(GlobalFlowRpcSvcApp.class);
    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<GlobalrpctestService> serviceRegistration;
    private GlobalFlowRpcService flowRpcService;

    public GlobalFlowRpcSvcApp(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry,
                               GlobalFlowRpcService flowRpcService) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.flowRpcService = flowRpcService;
    }

    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(GlobalrpctestService.class,
                new GlobalrpctestServiceImpl(flowRpcService));
        LOG.info("GlobalFlowRpcSvcApp Session Initiated");
    }

    public void close() {
        serviceRegistration.close();
        flowRpcService.close();
        LOG.info("GlobalFlowRpcSvcApp Closed");
    }
}
