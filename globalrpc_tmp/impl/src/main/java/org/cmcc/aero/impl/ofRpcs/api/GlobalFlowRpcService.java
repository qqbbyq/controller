/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.cmcc.aero.impl.ofRpcs.api;

import org.cmcc.aero.impl.rpc.message.GlobalRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.UpdateGroupsBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/8/10.
 */
public interface GlobalFlowRpcService{

    public Future<GlobalRpcResult> addFlowsBatch(AddFlowsBatchInput input);

    public Future<GlobalRpcResult> removeFlowsBatch(RemoveFlowsBatchInput input);

    public Future<GlobalRpcResult> updateFlowsBatch(UpdateFlowsBatchInput input);

    public Future<GlobalRpcResult> removeMetersBatch(RemoveMetersBatchInput input);

    public Future<GlobalRpcResult> updateMetersBatch(UpdateMetersBatchInput input);

    public Future<GlobalRpcResult> addMetersBatch(AddMetersBatchInput input);

    public Future<GlobalRpcResult> addGroupsBatch(AddGroupsBatchInput input);

    public Future<GlobalRpcResult> removeGroupsBatch(RemoveGroupsBatchInput input);

    public Future<GlobalRpcResult> updateGroupsBatch(UpdateGroupsBatchInput input);

    public void close();

    public List<String> listLocalMasteredNodes();
}
