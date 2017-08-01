/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.testApps;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.Food;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.Fruits;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.Vegetables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.Fruit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.FruitBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.FruitKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.vegetables.Vegetable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.vegetables.VegetableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.vegetables.VegetableKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by zhuyuqing on 2017/7/21.
 */
public class DataCreator {

    private final Logger LOG = LoggerFactory.getLogger(DataCreator.class);
    private DataBroker dataBroker;

    public DataCreator(DataBroker dataBroker){
        this.dataBroker = dataBroker;
    }


    public FruitKey createFruit(String name){
        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        FruitKey fruitKey = new FruitKey(name);
        FruitBuilder fruit = new FruitBuilder().setKey(fruitKey).setName(name);

        writeTx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Food.class)
          .child(Fruits.class).child(Fruit.class, fruitKey ), fruit.build());
        writeTx.submit();
        return fruitKey;
    }

    public VegetableKey createVegetable(String name){
        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
        VegetableKey fruitKey = new VegetableKey(name);
        VegetableBuilder fruit = new VegetableBuilder().setKey(fruitKey).setName(name);

        writeTx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Food.class)
          .child(Vegetables.class).child(Vegetable.class, fruitKey ), fruit.build());
        writeTx.submit();
        return fruitKey;
    }

    public void createAndDeleteFruit() throws InterruptedException{
        FruitKey fruitKey = createFruit("pear" + new Random().nextInt());
        Thread.sleep(2000);
        ReadWriteTransaction writeTx = dataBroker.newReadWriteTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Food.class)
          .child(Fruits.class).child(Fruit.class, fruitKey));
        writeTx.submit();
    }

    public void createAndUpdateFruit() throws InterruptedException {
        FruitKey fruitKey = createFruit("orange" + new Random().nextInt());
        Thread.sleep(2000);
        FruitBuilder fruit = new FruitBuilder()
          .setKey(fruitKey)
          .setName("banana" + new Random().nextInt());

        ReadWriteTransaction writeTx = dataBroker.newReadWriteTransaction();
        writeTx.merge(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Food.class)
          .child(Fruits.class).child(Fruit.class, fruitKey), fruit.build());
        writeTx.submit();
    }

//    public NetworkKey createNetwork(){
//        String networkID = UUID.randomUUID().toString();
//        LOG.info("Catty Test: random network uuid is {}",networkID);
//        NetworkKey networkKey = new NetworkKey(new Uuid(networkID));
//        WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
//        NetworkBuilder networkBuilder = new NetworkBuilder().setKey(networkKey).
//                setUuid(new Uuid(networkID)).
//                setName(networkID.substring(0, 8)).setTenantId(new Uuid("12623ddd-7859-4e07-b69c-4e939d2bfbaa"));
//
//        NetworkProviderExtensionBuilder networkProvider = new NetworkProviderExtensionBuilder();
//        SegmentsBuilder segment = new SegmentsBuilder();
//        segment.setNetworkType(NetworkTypeVxlan.class);
//        segment.setSegmentationId(Integer.toString(new Random().nextInt()));
//        segment.setKey(new SegmentsKey(0L));
//        networkProvider.setSegments(Arrays.asList(segment.build()));
//        networkBuilder.addAugmentation(NetworkProviderExtension.class, networkProvider.build());
//
//        writeTx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Neutron.class)
//                        .child(Networks.class).child(Network.class, networkKey),
//                networkBuilder.build());
//        writeTx.submit();
//        return networkKey;
//    }

//    public void createAndDeleteNetwork() throws InterruptedException{
//        NetworkKey networkKey = createNetwork();
//        Thread.sleep(2000);
//        ReadWriteTransaction writeTx = dataBroker.newReadWriteTransaction();
//        writeTx.delete(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Neutron.class)
//                .child(Networks.class).child(Network.class, networkKey));
//        writeTx.submit();
//    }
//
//    public void createAndUpdateNetwork() throws InterruptedException {
//        NetworkKey networkKey = createNetwork();
//        Thread.sleep(2000);
//        NetworkBuilder networkBuilder = new NetworkBuilder().setKey(networkKey).setName("ilovecatty");
//        ReadWriteTransaction writeTx = dataBroker.newReadWriteTransaction();
//        writeTx.merge(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Neutron.class)
//                .child(Networks.class).child(Network.class, networkKey), networkBuilder.build());
//        writeTx.submit();
//    }

}
