/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.cluster.databroker.actors.dds;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.controller.cluster.access.concepts.ClientIdentifier;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Client interface for interacting with the frontend actor. This interface is the primary access point through
 * which the DistributedDataStore frontend interacts with backend Shards.
 *
 * Keep this interface as clean as possible, as it needs to be implemented in thread-safe and highly-efficient manner.
 *  线程安全和高效
 * @author Robert Varga
 */
@Beta
//客户与前台actor交互的接口，这是前台与后台shards交互的主要接入点，用ClientIdentifier唯一标示
public interface DistributedDataStoreClient extends Identifiable<ClientIdentifier>, AutoCloseable {
    @Override
    @Nonnull ClientIdentifier getIdentifier();

    @Override
    void close();

    /**
     * Create a new local history. ClientLocalHistory represents the interface exposed to the client.
     *
     * @return Client history handle
     */
    @Nonnull ClientLocalHistory createLocalHistory();

    /**
     * Create a new free-standing transaction.
     *
     * @return Client transaction handle
     */
    @Nonnull ClientTransaction createTransaction();
}
