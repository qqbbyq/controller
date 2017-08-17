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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by cmcc on 2017/8/16.
 */
public interface OFStoreService {

    public void batchRemoveFlows(InstanceIdentifier nodeId, List<String> removeFlowIds);

    public void addFlowEntry(InstanceIdentifier nodeIdent, Short tableId, String flowId, Flow flowInput);

    public void batchRemoveMeters(InstanceIdentifier targetNode, List<Long> removedMeterIds);

    public void addMeter(InstanceIdentifier nodeIdent, Long meterId, Meter meterInput);

    public void addGroup(InstanceIdentifier nodeIdent, Long groupId, Group groupInput);

    public void batchRemovceGroups(InstanceIdentifier targetNode, List<Long> removedGroupIds);

    public String getFlowString(String nodeId, String flowId);

    public String getGroupString(String nodeId, String groupId);

    public String getMeterString(String nodeId, String meterId);
}
