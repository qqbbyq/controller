/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.UpdateFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/7/14.
 */
public interface OFFwdService{

    public Future<RpcResult<RemoveFlowsBatchOutput>> removeFlowsBatch(YangInstanceIdentifier path, NormalizedNode node);

    public Future<RpcResult<UpdateFlowsBatchOutput>> updateFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                      NormalizedNode normalNode);

    public Future<RpcResult<AddFlowsBatchOutput>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                                NormalizedNode normalNode);

    public Future<RpcResult<RemoveMetersBatchOutput>> removeMetersBatch(RemoveMetersBatchInput input);

    public Future<RpcResult<UpdateMetersBatchOutput>> updateMetersBatch(UpdateMetersBatchInput input);

    public Future<RpcResult<AddMetersBatchOutput>> addMetersBatch(AddMetersBatchInput input);

    public Future<RpcResult<AddGroupsBatchOutput>> addGroupsBatch(AddGroupsBatchInput input);

    public Future<RpcResult<RemoveGroupsBatchOutput>> removeGroupsBatch(RemoveGroupsBatchInput input);

    public Future<RpcResult<UpdateGroupsBatchOutput>> updateGroupsBatch(UpdateGroupsBatchInput input);

    public void close();

    public List<String> listLocalMasteredNodes();
}
