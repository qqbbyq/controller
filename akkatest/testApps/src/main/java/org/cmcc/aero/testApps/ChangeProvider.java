/*
 * Copyright © 2016 cmcc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.testApps;

import javafx.scene.control.TextFormatter;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.Fruits;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.akkatest.rev150105.food.fruits.Fruit;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Future;

public class ChangeProvider
//  implements AkkatestService
//  implements BindingAwareProvider, AutoCloseable
{

    private static final Logger LOG = LoggerFactory.getLogger(ChangeProvider.class);

    private final DataBroker dataBroker;

    private final RpcProviderRegistry rpcProviderRegistry;//new 猜测用作保存注入的rpc登记处的引用

    private RpcRegistration<AkkatestService> serviceRegistration; //new BA 猜测用作保存登记后的akkatestservice

//    @Override
//    public Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
//        HelloWorldOutputBuilder helloBuilder = new HelloWorldOutputBuilder();
//        try {
//            LOG.info("CANTEST: hello.input={}", input.getName());
//            if(Objects.equals(input.getName(), "0")){
//                DataCreator creator = new DataCreator(dataBroker);
//                creator.createFruit("apple" + new Random().nextInt());
//
//            } else if(Objects.equals(input.getName(), "1")){
//                DataCreator creator = new DataCreator(dataBroker);
//                creator.createAndUpdateFruit();
//
//            } else if (Objects.equals(input.getName(), "2")) {
//                DataCreator creator = new DataCreator(dataBroker);
//                creator.createAndDeleteFruit();
//
//            }else if(Objects.equals(input.getName(), "3")){
//                DataCreator creator = new DataCreator(dataBroker);
//                creator.createVegetable("tomato" + new Random().nextInt());
//            } else {
//                LOG.warn("CANTEST: hello.input does not invoke any operation, input={}", input.getName());
//
//            }
//            helloBuilder.setGreeting("Hello " + input.getName());
//            return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
//
//        } catch (Exception e){
//            LOG.error("CANTEST: error {}", e.getMessage());
//            e.printStackTrace();
//
//            helloBuilder.setGreeting("ERROR " + input.getName());
//            return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
//
//        }
//    }
//


    //    private ListenerRegistration<DataChangeListener> dataChangeListenerRegistration = null;
    //new rpcRegistry to inject RpcRegistry's reference
    public ChangeProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }


    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
//        serviceRegistration = rpcProviderRegistry.addRpcImplementation(AkkatestService.class, this);//new 猜测向rpc登记处增加akkatestService的登记
        LOG.info("ChangeProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
//        serviceRegistration.close();//new 关闭akkatestService
        LOG.info("ChangeProvider Closed");
    }
}