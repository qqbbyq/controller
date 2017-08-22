/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmcc on 2017/8/18.
 */
public class BatchMeterResults extends AbstracGlobalRpcResults{
    private static final int BATCH_METER_ERROR = 501;

    public List<BatchFailedMeters> getBatchFailedMetersList() {
        return batchFailedMetersList;
    }

    public void setBatchFailedMetersList(List<BatchFailedMeters> batchFailedMetersList) {
        this.batchFailedMetersList = batchFailedMetersList;
    }

    List<BatchFailedMeters> batchFailedMetersList;
    private BatchMeterResults(int errorCode){
        setErrorCode(errorCode);
    }

    public static BatchMeterResults success() {
        return new BatchMeterResults(SUCCESS_CODE);
    }

    public BatchMeterResults(){

    }

    public BatchMeterResults(AddMetersBatchOutput addOutput){
        List<BatchFailedMetersOutput> batchFailedMetersOutputs = addOutput.getBatchFailedMetersOutput();
        initBatchFailedMetersList(batchFailedMetersOutputs);
    }

    public BatchMeterResults(RemoveMetersBatchOutput removeOutput){
        List<BatchFailedMetersOutput> batchFailedMetersOutputs = removeOutput.getBatchFailedMetersOutput();
        initBatchFailedMetersList(batchFailedMetersOutputs);
    }

    private void initBatchFailedMetersList(List<BatchFailedMetersOutput> batchFailedMetersOutputs) {
        if(batchFailedMetersOutputs !=null){
            batchFailedMetersList = new ArrayList<>();
            for(BatchFailedMetersOutput output : batchFailedMetersOutputs){
                batchFailedMetersList.add(new BatchFailedMeters(output));
            }
            setErrorCode(BATCH_METER_ERROR);
        }else {
            setErrorCode(SUCCESS_CODE);
        }
    }
}
