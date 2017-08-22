/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;

import java.io.Serializable;

/**
 * Created by cmcc on 2017/8/18.
 */
public class BatchFailedGroups implements Serializable{
    private Integer batchOrder;
    private Long groupId;
    private Integer batchKey;

    public BatchFailedGroups(){

    }

    public BatchFailedGroups(Integer batchOrder, Long groupId, Integer batchKey){
        this.batchOrder = batchOrder;
        this.batchKey = batchKey;
        this.groupId = groupId;
    }

    public BatchFailedGroups(BatchFailedGroupsOutput batchFailedGroupsOutput){
        this.batchKey = batchFailedGroupsOutput.getKey().getBatchOrder();
        this.batchOrder = batchFailedGroupsOutput.getBatchOrder();
        this.groupId = batchFailedGroupsOutput.getGroupId().getValue();
    }

    public Integer getBatchOrder() {
        return batchOrder;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Integer getBatchKey() {
        return batchKey;
    }
}
