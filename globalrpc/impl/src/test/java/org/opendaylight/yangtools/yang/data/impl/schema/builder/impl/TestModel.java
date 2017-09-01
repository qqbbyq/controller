/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.*;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.*;

public class TestModel {

    public static final QName TEST_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test",
            "2014-03-13", "test");

    public static final QName AUG_NAME_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:aug",
            "2014-03-13", "name");

    public static final QName AUG_CONT_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:aug",
            "2014-03-13", "cont");


    public static final QName DESC_QNAME = QName.create(TEST_QNAME, "desc");
    public static final QName POINTER_QNAME = QName.create(TEST_QNAME, "pointer");
    public static final QName SOME_BINARY_DATA_QNAME = QName.create(TEST_QNAME, "some-binary-data");
    public static final QName BINARY_LEAF_LIST_QNAME = QName.create(TEST_QNAME, "binary_leaf_list");
    public static final QName SOME_REF_QNAME = QName.create(TEST_QNAME, "some-ref");
    public static final QName MYIDENTITY_QNAME = QName.create(TEST_QNAME, "myidentity");
    public static final QName SWITCH_FEATURES_QNAME = QName.create(TEST_QNAME, "switch-features");

    public static final QName AUGMENTED_LIST_QNAME = QName.create(TEST_QNAME, "augmented-list");
    public static final QName AUGMENTED_LIST_ENTRY_QNAME = QName.create(TEST_QNAME, "augmented-list-entry");

    public static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    public static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    public static final QName INNER_CONTAINER_QNAME = QName.create(TEST_QNAME, "inner-container");
    public static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice");
    public static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    public static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    public static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value");
    public static final QName BOOLEAN_LEAF_QNAME = QName.create(TEST_QNAME, "boolean-leaf");
    public static final QName SHORT_LEAF_QNAME = QName.create(TEST_QNAME, "short-leaf");
    public static final QName BYTE_LEAF_QNAME = QName.create(TEST_QNAME, "byte-leaf");
    public static final QName BIGINTEGER_LEAF_QNAME = QName.create(TEST_QNAME, "biginteger-leaf");
    public static final QName BIGDECIMAL_LEAF_QNAME = QName.create(TEST_QNAME, "bigdecimal-leaf");
    public static final QName ORDERED_LIST_QNAME = QName.create(TEST_QNAME, "ordered-list");
    public static final QName ORDERED_LIST_ENTRY_QNAME = QName.create(TEST_QNAME, "ordered-list-leaf");
    public static final QName UNKEYED_LIST_QNAME = QName.create(TEST_QNAME, "unkeyed-list");
    public static final QName UNKEYED_LIST_ENTRY_QNAME = QName.create(TEST_QNAME, "unkeyed-list-entry");
    public static final QName CHOICE_QNAME = QName.create(TEST_QNAME, "choice");
    public static final QName SHOE_QNAME = QName.create(TEST_QNAME, "shoe");
    public static final QName ANY_XML_QNAME = QName.create(TEST_QNAME, "any");
    public static final QName INVALID_QNAME = QName.create(TEST_QNAME, "invalid");
    private static final String DATASTORE_TEST_YANG = "/odl-datastore-test.yang";
    private static final String DATASTORE_AUG_YANG = "/odl-datastore-augmentation.yang";
    private static final String DATASTORE_TEST_NOTIFICATION_YANG = "/odl-datastore-test-notification.yang";


    public static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    public static final YangInstanceIdentifier DESC_PATH = YangInstanceIdentifier.
            builder(TEST_PATH).node(DESC_QNAME).build();
    public static final YangInstanceIdentifier OUTER_LIST_PATH =
            YangInstanceIdentifier.builder(TEST_PATH).node(OUTER_LIST_QNAME).build();
    public static final QName TWO_THREE_QNAME = QName.create(TEST_QNAME, "two");
    public static final QName TWO_QNAME = QName.create(TEST_QNAME, "two");
    public static final QName THREE_QNAME = QName.create(TEST_QNAME, "three");

    private static final Integer ONE_ID = 1;
    private static final Integer TWO_ID = 2;
    private static final String TWO_ONE_NAME = "one";
    private static final String TWO_TWO_NAME = "two";
    private static final String DESC = "Hello there";
    private static final Long LONG_ID = 1L;
    private static final Boolean ENABLED = true;
    private static final Short SHORT_ID = 1;
    private static final Byte BYTE_ID = 1;
    // Family specific constants
    public static final QName FAMILY_QNAME = QName.create(
            "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:notification-test", "2014-04-17", "family");
    public static final QName CHILDREN_QNAME = QName.create(FAMILY_QNAME, "children");
    public static final QName GRAND_CHILDREN_QNAME = QName.create(FAMILY_QNAME, "grand-children");
    public static final QName CHILD_NUMBER_QNAME = QName.create(FAMILY_QNAME, "child-number");
    public static final QName CHILD_NAME_QNAME = QName.create(FAMILY_QNAME, "child-name");
    public static final QName GRAND_CHILD_NUMBER_QNAME = QName.create(FAMILY_QNAME, "grand-child-number");
    public static final QName GRAND_CHILD_NAME_QNAME = QName.create(FAMILY_QNAME,"grand-child-name");

    public static final YangInstanceIdentifier FAMILY_PATH = YangInstanceIdentifier.of(FAMILY_QNAME);
    public static final YangInstanceIdentifier FAMILY_DESC_PATH =
            YangInstanceIdentifier.builder(FAMILY_PATH).node(DESC_QNAME).build();
    public static final YangInstanceIdentifier CHILDREN_PATH =
            YangInstanceIdentifier.builder(FAMILY_PATH).node(CHILDREN_QNAME).build();

    private static final Integer FIRST_CHILD_ID = 1;
    private static final Integer SECOND_CHILD_ID = 2;

    private static final String FIRST_CHILD_NAME = "first child";
    private static final String SECOND_CHILD_NAME = "second child";

    private static final Integer FIRST_GRAND_CHILD_ID = 1;
    private static final Integer SECOND_GRAND_CHILD_ID = 2;

    private static final String FIRST_GRAND_CHILD_NAME = "first grand child";
    private static final String SECOND_GRAND_CHILD_NAME = "second grand child";

    private static final MapEntryNode BAR_NODE = mapEntryBuilder(
            OUTER_LIST_QNAME, ID_QNAME, TWO_ID) //
            .withChild(mapNodeBuilder(INNER_LIST_QNAME) //
                    .withChild(mapEntry(INNER_LIST_QNAME, NAME_QNAME, TWO_ONE_NAME)) //
                    .withChild(mapEntry(INNER_LIST_QNAME, NAME_QNAME, TWO_TWO_NAME)) //
                    .build()) //
            .build();

    public static final InputStream getDatastoreTestInputStream() {
        return getInputStream(DATASTORE_TEST_YANG);
    }

    public static final InputStream getDatastoreAugInputStream() {
        return getInputStream(DATASTORE_AUG_YANG);
    }

    public static final InputStream getDatastoreTestNotificationInputStream() {
        return getInputStream(DATASTORE_TEST_NOTIFICATION_YANG);
    }

    private static InputStream getInputStream(final String resourceName) {
        return TestModel.class.getResourceAsStream(resourceName);
    }

    public static SchemaContext createTestContext() {
        List<InputStream> inputStreams = new ArrayList<>();
        inputStreams.add(getDatastoreTestInputStream());
        inputStreams.add(getDatastoreAugInputStream());
        inputStreams.add(getDatastoreTestNotificationInputStream());

        return resolveSchemaContext(inputStreams);
    }

    public static SchemaContext createTestContextWithoutTestSchema() {
        List<InputStream> inputStreams = new ArrayList<>();
        inputStreams.add(getDatastoreTestNotificationInputStream());

        return resolveSchemaContext(inputStreams);
    }


    public static SchemaContext createTestContextWithoutAugmentationSchema() {
        List<InputStream> inputStreams = new ArrayList<>();
        inputStreams.add(getDatastoreTestInputStream());
        inputStreams.add(getDatastoreTestNotificationInputStream());

        return resolveSchemaContext(inputStreams);
    }

    private static SchemaContext resolveSchemaContext(List<InputStream> streams) {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        final SchemaContext schemaContext;

        try {
            schemaContext = reactor.buildEffective(streams);
        } catch (ReactorException e) {
            throw new RuntimeException("Unable to build schema context from " + streams, e);
        }
        return schemaContext;
    }

    /**
     * Returns a test document
     * <p/>
     * <p/>
     * <pre>
     * test
     *     outer-list
     *          id 1
     *     outer-list
     *          id 2
     *          inner-list
     *                  name "one"
     *          inner-list
     *                  name "two"
     *
     * </pre>
     *
     * @return
     */
    public static NormalizedNode<?, ?> createDocumentOne(
            SchemaContext schemaContext) {
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(
                        new NodeIdentifier(schemaContext.getQName()))
                .withChild(createTestContainer()).build();

    }

    public static DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> createBaseTestContainerBuilder() {
        // Create a list of shoes
        // This is to test leaf list entry
        final LeafSetEntryNode<Object> nike = ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                new NodeWithValue<>(SHOE_QNAME, "nike")).withValue("nike").build();

        final LeafSetEntryNode<Object> puma = ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(
                new NodeWithValue<>(SHOE_QNAME, "puma")).withValue("puma").build();

        final LeafSetNode<Object> shoes = ImmutableLeafSetNodeBuilder.create().withNodeIdentifier(
                new NodeIdentifier(SHOE_QNAME)).withChild(nike).withChild(puma).build();

        // Test a leaf-list where each entry contains an identity
        final LeafSetEntryNode<Object> cap1 =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "capability"), DESC_QNAME))
                        .withValue(DESC_QNAME).build();

        final LeafSetNode<Object> capabilities =
                ImmutableLeafSetNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(QName.create(
                                        TEST_QNAME, "capability"))).withChild(cap1).build();

        ContainerNode switchFeatures =
                ImmutableContainerNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(SWITCH_FEATURES_QNAME))
                        .withChild(capabilities).build();

        // Create a leaf list with numbers
        final LeafSetEntryNode<Object> five =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                (new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "number"), 5))).withValue(5).build();
        final LeafSetEntryNode<Object> fifteen =
                ImmutableLeafSetEntryNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                (new NodeWithValue<>(QName.create(
                                        TEST_QNAME, "number"), 15))).withValue(15).build();
        final LeafSetNode<Object> numbers =
                ImmutableLeafSetNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(QName.create(
                                        TEST_QNAME, "number"))).withChild(five).withChild(fifteen)
                        .build();


        // Create augmentations
        MapEntryNode augMapEntry = createAugmentedListEntry(1, "First Test");

        // Create a bits leaf
        NormalizedNodeAttrBuilder<NodeIdentifier, Object, LeafNode<Object>>
                myBits = Builders.leafBuilder()
                .withNodeIdentifier(new NodeIdentifier(QName.create(TEST_QNAME, "my-bits")))
                .withValue(ImmutableSet.of("foo", "bar"));

        // Create unkeyed list entry
        UnkeyedListEntryNode unkeyedListEntry =
                Builders.unkeyedListEntryBuilder().withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME)).
                        withChild(ImmutableNodes.leafNode(NAME_QNAME, "unkeyed-entry-name")).build();

        // Create YangInstanceIdentifier with all path arg types.
        YangInstanceIdentifier instanceID = YangInstanceIdentifier.create(
                new NodeIdentifier(QName.create(TEST_QNAME, "qname")),
                new NodeIdentifierWithPredicates(QName.create(TEST_QNAME, "list-entry"),
                        QName.create(TEST_QNAME, "key"), 10),
                new AugmentationIdentifier(ImmutableSet.of(
                        QName.create(TEST_QNAME, "aug1"), QName.create(TEST_QNAME, "aug2"))),
                new NodeWithValue<>(QName.create(TEST_QNAME, "leaf-list-entry"), "foo"));

        Map<QName, Object> keyValues = new HashMap<>();
        keyValues.put(CHILDREN_QNAME, FIRST_CHILD_NAME);


        // Create the document
        return ImmutableContainerNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(TEST_QNAME))
                .withChild(myBits.build())
                .withChild(ImmutableNodes.leafNode(DESC_QNAME, DESC))
                .withChild(ImmutableNodes.leafNode(BOOLEAN_LEAF_QNAME, ENABLED))
                .withChild(ImmutableNodes.leafNode(SHORT_LEAF_QNAME, SHORT_ID))
                .withChild(ImmutableNodes.leafNode(BYTE_LEAF_QNAME, BYTE_ID))
                .withChild(ImmutableNodes.leafNode(TestModel.BIGINTEGER_LEAF_QNAME, BigInteger.valueOf(100)))
                .withChild(ImmutableNodes.leafNode(TestModel.BIGDECIMAL_LEAF_QNAME, BigDecimal.valueOf(1.2)))
                .withChild(ImmutableNodes.leafNode(SOME_REF_QNAME, instanceID))
                .withChild(ImmutableNodes.leafNode(MYIDENTITY_QNAME, DESC_QNAME))
                .withChild(Builders.unkeyedListBuilder()
                        .withNodeIdentifier(new NodeIdentifier(UNKEYED_LIST_QNAME))
                        .withChild(unkeyedListEntry).build())
                .withChild(Builders.choiceBuilder()
                        .withNodeIdentifier(new NodeIdentifier(TWO_THREE_QNAME))
                        .withChild(ImmutableNodes.leafNode(TWO_QNAME, "two")).build())
                .withChild(Builders.orderedMapBuilder().
                        withNodeIdentifier(new NodeIdentifier(ORDERED_LIST_QNAME)).
                        withValue(ImmutableList.<MapEntryNode>builder().add(
                                mapEntryBuilder(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "1").build(),
                                mapEntryBuilder(ORDERED_LIST_QNAME, ORDERED_LIST_ENTRY_QNAME, "2").build()).build()).build())
                .withChild(shoes)
                .withChild(numbers)
                .withChild(switchFeatures)
                .withChild(mapNodeBuilder(AUGMENTED_LIST_QNAME).withChild(augMapEntry).build())
                .withChild(mapNodeBuilder(OUTER_LIST_QNAME)
                                .withChild(mapEntry(OUTER_LIST_QNAME, ID_QNAME, ONE_ID))
                                .withChild(BAR_NODE).build()
                );
    }

    public static ContainerNode createTestContainer() {
        return createBaseTestContainerBuilder().build();
    }

    public static MapEntryNode createAugmentedListEntry(int id, String name) {

        Set<QName> childAugmentations = new HashSet<>();
        childAugmentations.add(AUG_CONT_QNAME);

        ContainerNode augCont =
                ImmutableContainerNodeBuilder
                        .create()
                        .withNodeIdentifier(
                                new NodeIdentifier(AUG_CONT_QNAME))
                        .withChild(ImmutableNodes.leafNode(AUG_NAME_QNAME, name)).build();


        final AugmentationIdentifier augmentationIdentifier =
                new AugmentationIdentifier(childAugmentations);

        final AugmentationNode augmentationNode =
                Builders.augmentationBuilder()
                        .withNodeIdentifier(augmentationIdentifier).withChild(augCont)
                        .build();

        return ImmutableMapEntryNodeBuilder
                .create()
                .withNodeIdentifier(
                        new NodeIdentifierWithPredicates(
                                AUGMENTED_LIST_QNAME, ID_QNAME, id))
                .withChild(ImmutableNodes.leafNode(ID_QNAME, id))
                .withChild(augmentationNode).build();
    }


    public static ContainerNode createFamily() {
        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> familyContainerBuilder =
                ImmutableContainerNodeBuilder.create().withNodeIdentifier(
                        new NodeIdentifier(FAMILY_QNAME));

        final CollectionNodeBuilder<MapEntryNode, MapNode> childrenBuilder =
                mapNodeBuilder(CHILDREN_QNAME);

        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> firstChildBuilder =
                mapEntryBuilder(CHILDREN_QNAME, CHILD_NUMBER_QNAME, FIRST_CHILD_ID);
        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> secondChildBuilder =
                mapEntryBuilder(CHILDREN_QNAME, CHILD_NUMBER_QNAME, SECOND_CHILD_ID);

        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> firstGrandChildBuilder =
                mapEntryBuilder(GRAND_CHILDREN_QNAME, GRAND_CHILD_NUMBER_QNAME,
                        FIRST_GRAND_CHILD_ID);
        final DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> secondGrandChildBuilder =
                mapEntryBuilder(GRAND_CHILDREN_QNAME, GRAND_CHILD_NUMBER_QNAME,
                        SECOND_GRAND_CHILD_ID);

        firstGrandChildBuilder
                .withChild(
                        ImmutableNodes.leafNode(GRAND_CHILD_NUMBER_QNAME,
                                FIRST_GRAND_CHILD_ID)).withChild(
                ImmutableNodes.leafNode(GRAND_CHILD_NAME_QNAME,
                        FIRST_GRAND_CHILD_NAME));

        secondGrandChildBuilder.withChild(
                ImmutableNodes
                        .leafNode(GRAND_CHILD_NUMBER_QNAME, SECOND_GRAND_CHILD_ID))
                .withChild(
                        ImmutableNodes.leafNode(GRAND_CHILD_NAME_QNAME,
                                SECOND_GRAND_CHILD_NAME));

        firstChildBuilder
                .withChild(ImmutableNodes.leafNode(CHILD_NUMBER_QNAME, FIRST_CHILD_ID))
                .withChild(ImmutableNodes.leafNode(CHILD_NAME_QNAME, FIRST_CHILD_NAME))
                .withChild(
                        mapNodeBuilder(GRAND_CHILDREN_QNAME).withChild(
                                firstGrandChildBuilder.build()).build());


        secondChildBuilder
                .withChild(ImmutableNodes.leafNode(CHILD_NUMBER_QNAME, SECOND_CHILD_ID))
                .withChild(ImmutableNodes.leafNode(CHILD_NAME_QNAME, SECOND_CHILD_NAME))
                .withChild(
                        mapNodeBuilder(GRAND_CHILDREN_QNAME).withChild(
                                firstGrandChildBuilder.build()).build());

        childrenBuilder.withChild(firstChildBuilder.build());
        childrenBuilder.withChild(secondChildBuilder.build());

        return familyContainerBuilder.withChild(childrenBuilder.build()).build();
    }

}