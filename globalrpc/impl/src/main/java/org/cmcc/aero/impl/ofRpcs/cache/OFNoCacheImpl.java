/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.cache;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by cmcc on 2017/8/16.
 */
public class OFNoCacheImpl implements OFStoreService {
    private Logger LOG = LoggerFactory.getLogger(OFStoreServiceImpl.class);

    @Override
    public void batchRemoveFlows(InstanceIdentifier nodeId, List<String> removeFlowIds) {
        LOG.info("OFNoCacheImpl.batchRemoveFlows is called, nothing to do.");
    }

    @Override
    public void addFlowEntry(InstanceIdentifier nodeIdent, Short tableId, String flowId, Flow flowInput) {
        LOG.info("OFNoCacheImpl.addFlowEntry is called, nothing to do.");
    }

    @Override
    public void batchRemoveMeters(InstanceIdentifier targetNode, List<Long> removedMeterIds) {
        LOG.info("OFNoCacheImpl.batchRemoveMeters is called, nothing to do.");
    }

    @Override
    public void addMeter(InstanceIdentifier nodeIdent, Long meterId, Meter meterInput) {
        LOG.info("OFNoCacheImpl.addMeter is called, nothing to do.");
    }

    @Override
    public void addGroup(InstanceIdentifier nodeIdent, Long groupId, Group groupInput) {
        LOG.info("OFNoCacheImpl.addGroup is called, nothing to do.");
    }

    @Override
    public void batchRemovceGroups(InstanceIdentifier targetNode, List<Long> removedGroupIds) {
        LOG.info("OFNoCacheImpl.batchRemovceGroups is called, nothing to do.");
    }

    @Override
    public String getFlowString(String nodeId, String flowId){
        return "Not Implemented";
    }

    @Override
    public String getGroupString(String nodeId, String groupId) {
        return "Not Implemented";
    }

    @Override
    public String getMeterString(String nodeId, String meterId) {
        return "Not Implemented";
    }
}
