/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.ofRpcs.serialize;

import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.AddGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.RemoveGroupsBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groups.service.rev160315.batch.group.output.list.grouping.BatchFailedGroupsOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cmcc on 2017/8/18.
 */
public class BatchGroupResults extends AbstracGlobalRpcResults{
    private static final int BATCH_GROUP_ERROR = 502;

    public List<BatchFailedGroups> getBatchFailedGroupsList() {
        return batchFailedGroupsList;
    }

    public void setBatchFailedGroupsList(List<BatchFailedGroups> batchFailedGroupsList) {
        this.batchFailedGroupsList = batchFailedGroupsList;
    }

    private List<BatchFailedGroups> batchFailedGroupsList;

    private BatchGroupResults(int errorCode){
        setErrorCode(errorCode);
    }

    public static BatchGroupResults success() {
        return new BatchGroupResults(SUCCESS_CODE);
    }

    public BatchGroupResults(){

    }

    public BatchGroupResults(AddGroupsBatchOutput addGroupsBatchOutput){
        List<BatchFailedGroupsOutput> failedGroups = addGroupsBatchOutput.getBatchFailedGroupsOutput();
        initBatchFailedGroupLists(failedGroups);
    }

    public BatchGroupResults(RemoveGroupsBatchOutput remGroupsBatchOutput){
        List<BatchFailedGroupsOutput> failedGroups = remGroupsBatchOutput.getBatchFailedGroupsOutput();
        initBatchFailedGroupLists(failedGroups);
    }

    public void initBatchFailedGroupLists(List<BatchFailedGroupsOutput> batchFailedGroupLists) {
        if(batchFailedGroupLists != null && batchFailedGroupLists.isEmpty()){
            this.batchFailedGroupsList = new ArrayList<>();
            for(BatchFailedGroupsOutput output : batchFailedGroupLists){
                batchFailedGroupsList.add(new BatchFailedGroups(output));
            }
            setErrorCode(BATCH_GROUP_ERROR);
        }else {
            setErrorCode(SUCCESS_CODE);
        }
    }
}
