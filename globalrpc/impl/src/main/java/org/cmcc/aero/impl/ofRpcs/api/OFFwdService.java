/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.api;

import org.cmcc.aero.impl.ofRpcs.serialize.BatchFlowResults;
import org.cmcc.aero.impl.ofRpcs.serialize.BatchGroupResults;
import org.cmcc.aero.impl.ofRpcs.serialize.BatchMeterResults;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by cmcc on 2017/7/14.
 */
public interface OFFwdService{

    public Future<RpcResult<BatchFlowResults>> removeFlowsBatch(YangInstanceIdentifier path, NormalizedNode node);

    public Future<RpcResult<BatchFlowResults>> removeFlowsBatch(YangInstanceIdentifier path,
                                                                NormalizedNode node, boolean isBarrierAfter);

    public Future<RpcResult<BatchFlowResults>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                             NormalizedNode normalNode);

    public Future<RpcResult<BatchFlowResults>> addFlowsBatch(YangInstanceIdentifier nodeYangId,
                                                             NormalizedNode normalNode, boolean isBarrierAfter);

    public Future<RpcResult<BatchMeterResults>> removeMetersBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode);

    public Future<RpcResult<BatchMeterResults>> removeMetersBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode, boolean isBarrierAfter);

    public Future<RpcResult<BatchMeterResults>> addMetersBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode);

    public Future<RpcResult<BatchMeterResults>> addMetersBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode, boolean isBarrierAfter);

    public Future<RpcResult<BatchGroupResults>> addGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode);

    public Future<RpcResult<BatchGroupResults>> addGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                               NormalizedNode normalNode, boolean isBarrierAfter);

    public Future<RpcResult<BatchGroupResults>> removeGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode);

    public Future<RpcResult<BatchGroupResults>> removeGroupsBatch(YangInstanceIdentifier nodeYangId,
                                                                  NormalizedNode normalNode, boolean isBarrierAfter);

    public void close();

    public List<String> listLocalMasteredNodes();
}
