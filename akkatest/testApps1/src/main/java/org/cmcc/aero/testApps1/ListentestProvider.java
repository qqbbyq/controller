/*
 * Copyright © 2016 zhuyuqing copyright. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.testApps1;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.Food;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.Fruits;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.Fruit;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListentestProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ListentestProvider.class);

    private final DataBroker dataBroker;

    public ListentestProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        testDataBrokerAttributes(dataBroker);
    }


    private void testDataBrokerAttributes(final DataBroker dataBroker){
        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("CANTEST: testDataBrokerAttributes started");
                    LOG.info("CANTEST: DataBroker class is {}", dataBroker.getClass());
                    Thread.sleep(20000);
                    testRegisterListener();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        newThread.start();
    }

    private void testRegisterListener() throws InterruptedException {
        LOG.info("CANTEST: TestDbBroker testRegisterListener.");

        InstanceIdentifier<Fruit> path = InstanceIdentifier.create(Food.class).child(Fruits.class).
          child(Fruit.class);
        DataChangeListenerTestor dataChangeListenerTestor = new DataChangeListenerTestor();
        dataChangeListenerTestor.registerDataChangeListener(dataBroker, path);
        DataTreeChangeListenerTestor dataTreeChangeListenerTestor = new DataTreeChangeListenerTestor();
        dataTreeChangeListenerTestor.registerDataTreeChangeListener(dataBroker, path);

    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {

        LOG.info("ListentestProvider Session Initiated")
        ;
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("ListentestProvider Closed");
    }
}