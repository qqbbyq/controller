/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;

import java.io.Serializable;

/**
 * Created by cmcc on 2017/8/18.
 */
public class BatchFailedMeters implements Serializable{

    private Integer batchOrder;
    private Integer batchKey;
    private Long meterId;

    public BatchFailedMeters(){

    }

    public BatchFailedMeters(Integer batchOrder, Integer key, Long meterId){
        this.batchOrder = batchOrder;
        this.batchKey = key;
        this.meterId = meterId;
    }

    public BatchFailedMeters(BatchFailedMetersOutput batchFailedMetersOutput){
        this.batchOrder = batchFailedMetersOutput.getBatchOrder();
        this.batchKey = batchFailedMetersOutput.getKey().getBatchOrder();
        this.meterId = batchFailedMetersOutput.getMeterId().getValue();
    }

    public Integer getBatchOrder() {
        return batchOrder;
    }

    public Integer getBatchKey() {
        return batchKey;
    }

    public Long getMeterId() {
        return meterId;
    }

    public void setBatchOrder(Integer batchOrder) {
        this.batchOrder = batchOrder;
    }

    public void setBatchKey(Integer batchKey) {
        this.batchKey = batchKey;
    }

    public void setMeterId(Long meterId) {
        this.meterId = meterId;
    }
}
