/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;

import java.io.Serializable;

/**
 * Created by cmcc on 2017/8/17.
 */
public class BatchFailedFlows implements Serializable{
    private String flowId;
    private Integer batchOrder;
    private Integer batchKey;

    public BatchFailedFlows(){

    }

    public BatchFailedFlows(Integer batchOrder, String flowId, Integer batchKey){
        this.batchOrder = batchOrder;
        this.flowId = flowId;
        this.batchKey = batchKey;
    }

    public BatchFailedFlows(BatchFailedFlowsOutput batchFailedFlowsOutput){
        this.batchOrder = batchFailedFlowsOutput.getBatchOrder();
        this.flowId = batchFailedFlowsOutput.getFlowId().getValue();
        this.batchKey = batchFailedFlowsOutput.getKey().getBatchOrder();
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public Integer getBatchOrder() {
        return batchOrder;
    }

    public void setBatchOrder(Integer batchOrder) {
        this.batchOrder = batchOrder;
    }

    public Integer getBatchKey() {
        return batchKey;
    }

    public void setBatchKey(Integer batchKey) {
        this.batchKey = batchKey;
    }
}
