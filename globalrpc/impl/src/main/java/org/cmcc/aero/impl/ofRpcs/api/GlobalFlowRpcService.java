/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.ofRpcs.api;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/8/10.
 */
public interface GlobalFlowRpcService{

    public Future<GlobalRpcResult> addFlowsBatch(String nodeId, List<Flow> flows);

    public Future<GlobalRpcResult> removeFlowsBatch(String nodeId, List<Flow> flows);

    public Future<GlobalRpcResult> removeMetersBatch(String nodeId, List<Meter> meters);

    public Future<GlobalRpcResult> addMetersBatch(String nodeId, List<Meter> meters);

    public Future<GlobalRpcResult> addGroupsBatch(String nodeId, List<Group> groups);

    public Future<GlobalRpcResult> removeGroupsBatch(String nodeId, List<Group> groups);

    public void close();

    public List<String> listLocalMasteredNodes();

    public String getFlowString(String nodeId, String flowId);

    public String getGroupString(String nodeId, String flowId);

    public String getMeterString(String nodeId, String flowId);
}
