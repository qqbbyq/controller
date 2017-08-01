/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.testApps1;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.Fruits;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.Fruit;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cmcc on 2017/3/10.
 */
public class DataTreeChangeListenerTestor implements ClusteredDataTreeChangeListener<Fruit>{
    private Logger LOG = LoggerFactory.getLogger(DataTreeChangeListenerTestor.class);

    public DataTreeChangeListenerTestor(){

    }

    public void registerDataTreeChangeListener(DataBroker dataBroker, InstanceIdentifier path) {
        DataTreeIdentifier<Fruit> dataTreeIdentifier = new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, path) ;
        dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Fruit>> collection) {
        LOG.info("Catty Test: DataTreeChangeListenerTestor get message Networks DataTreeChanged");
        for(DataTreeModification<Fruit> networkModification : collection){
            DataObjectModification<Fruit> modification = networkModification.getRootNode();
            DataObjectModification.ModificationType modificationType = modification.getModificationType();
            if(modificationType.equals(DataObjectModification.ModificationType.WRITE)){
                Fruit after = modification.getDataAfter();
                LOG.info("Catty Test: DataTreeChangeListenerTestor create network {}", after);
            }else if(modificationType.equals(DataObjectModification.ModificationType.DELETE)){
                Fruit before = modification.getDataBefore();
                LOG.info("Catty Test: DataTreeChangeListenerTestor delete network {}", before);
            }else if(modificationType.equals(DataObjectModification.ModificationType.SUBTREE_MODIFIED)){
                Fruit before = modification.getDataBefore();
                Fruit after = modification.getDataAfter();
                LOG.info("Catty Test: DataTreeChangeListenerTestor update network, before {}, " +
                        "after {}", before, after);
            }
        }
    }

    private void testNetworksPath(Fruits before, Fruits after){
        Map<String, Fruit> beforeNetworks = makeNetworkMaps(before.getFruit());
        Map<String, Fruit> afterNetworks = makeNetworkMaps(after.getFruit());
        for(String netID : beforeNetworks.keySet()){
            if(!afterNetworks.keySet().contains(netID)){
                LOG.info("Catty Test: DataTreeChangeListenerTestor delete network {}",
                        beforeNetworks.get(netID));
            }else if(afterNetworks.get(netID).equals(beforeNetworks.get(netID))){
                LOG.info("Catty Test: DataTreeChangeListenerTestor update network {}, before {}, " +
                        "after {}", netID,  beforeNetworks.get(netID),afterNetworks.get(netID));
            }
        }

        for(String netID : afterNetworks.keySet()){
            if(!beforeNetworks.keySet().contains(netID)){
                LOG.info("Catty Test: DataTreeChangeListenerTestor create network {}", afterNetworks.get(netID));
            }
        }
    }

    private Map<String, Fruit> makeNetworkMaps(List<Fruit> networks) {
        Map<String, Fruit> netMaps = new HashMap<>();
        for(Fruit net: networks){
            netMaps.put(net.getName().toString(), net);
        }
        return netMaps;
    }
}
