/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.testApps1;

import org.opendaylight.controller.md.sal.binding.api.ClusteredDataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Created by cmcc on 2017/3/10.
 */
public class DataChangeListenerTestor implements ClusteredDataChangeListener {
    private Logger LOG = LoggerFactory.getLogger(DataChangeListenerTestor.class);

    private LogicalDatastoreType store = LogicalDatastoreType.CONFIGURATION;

    public DataChangeListenerTestor(){

    }

    public void registerDataChangeListener(DataBroker dataBroker, InstanceIdentifier path){
        dataBroker.registerDataChangeListener(store, path, this, AsyncDataBroker.DataChangeScope.ONE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> asyncDataChangeEvent) {

        Map<InstanceIdentifier<?>, DataObject> created = asyncDataChangeEvent.getCreatedData();
        Map<InstanceIdentifier<?>, DataObject> updated = asyncDataChangeEvent.getUpdatedData();
        Set<InstanceIdentifier<?>> deleted = asyncDataChangeEvent.getRemovedPaths();
        LOG.info("Catty Test: DataChangeListenerTestor get message Fruit DataTreeChanged, created {}," +
                        "updated {}, delted {}", created.size(), updated.size(), deleted.size());
    }
}
