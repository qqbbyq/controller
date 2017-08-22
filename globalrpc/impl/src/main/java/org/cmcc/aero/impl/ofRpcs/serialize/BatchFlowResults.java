/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.AddFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.RemoveFlowsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flows.service.rev160314.batch.flow.output.list.grouping.BatchFailedFlowsOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmcc on 2017/8/17.
 */
public class BatchFlowResults extends AbstracGlobalRpcResults {
    private static final int BATCH_FLOW_ERROR = 500;

    private List<BatchFailedFlows> failedFlowsList;

    private BatchFlowResults(int errorCode) {
        setErrorCode(errorCode);
    }

    public BatchFlowResults(){}

    public List<BatchFailedFlows> getFailedFlowsList() {
        return failedFlowsList;
    }

    public void setFailedFlowsList(List<BatchFailedFlows> failedFlowsList) {
        setErrorCode(BATCH_FLOW_ERROR);
        this.failedFlowsList = failedFlowsList;
    }

    public BatchFlowResults(List<BatchFailedFlows> batchFailedFlows){
        setFailedFlowsList(batchFailedFlows);
    }

    public BatchFlowResults(RemoveFlowsBatchOutput removeFlowsBatchOutput){
        List<BatchFailedFlowsOutput> failedFlows = removeFlowsBatchOutput.getBatchFailedFlowsOutput();
        initBatchFailedFlowsList(failedFlows);
    }

    private void initBatchFailedFlowsList(List<BatchFailedFlowsOutput> failedFlows) {
        if( failedFlows!= null){
            List<BatchFailedFlows> failureList = new ArrayList<>();
            for(BatchFailedFlowsOutput failedFlow : failedFlows){
                failureList.add(new BatchFailedFlows(failedFlow));
            }
            setFailedFlowsList(failureList);
        }else {
            setErrorCode(SUCCESS_CODE);
        }
    }

    public BatchFlowResults(AddFlowsBatchOutput addFlowsBatchOutput){
        List<BatchFailedFlowsOutput> failedFlows = addFlowsBatchOutput.getBatchFailedFlowsOutput();
        initBatchFailedFlowsList(failedFlows);
    }

    public static BatchFlowResults success() {
        return new BatchFlowResults(SUCCESS_CODE);
    }
}
