/*
 * Copyright © 2017 CMCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.cmcc.aero.impl.utils;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.cluster.datastore.utils.SerializationUtils;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodecFactory;
import org.opendaylight.controller.sal.core.api.model.SchemaService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by cmcc on 2017/8/15.
 */
public class NodeNormalizedCodecUtils implements SchemaContextListener, AutoCloseable {
    private SchemaContext schemaContext;
    private JSONCodecFactory jsonCodecFactory;
    private BindingToNormalizedNodeCodec codec;
    private ListenerRegistration<SchemaContextListener> contextListener;

    public NodeNormalizedCodecUtils(SchemaService schemaService, ClassLoadingStrategy classLoadingStrategy){
        contextListener = schemaService.registerSchemaContextListener(this);
        this.jsonCodecFactory = JSONCodecFactory.create(schemaContext);
        this.codec = BindingToNormalizedNodeCodecFactory.newInstance(classLoadingStrategy);
        BindingToNormalizedNodeCodecFactory.registerInstance(this.codec, schemaService);
    }

    public NodeNormalizedCodecUtils(SchemaContext initSchemaContext, ClassLoadingStrategy classLoadingStrategy){
        this.schemaContext = schemaContext;
        this.jsonCodecFactory = JSONCodecFactory.create(schemaContext);
        this.codec = BindingToNormalizedNodeCodecFactory.newInstance(classLoadingStrategy);
    }

    public SchemaContext getSchemaContext(){
        return schemaContext;
    }

    public BindingToNormalizedNodeCodec getCodec(){
        return codec;
    }

    private String normalizedNodeToJsonStreamTransformation(final Writer writer, URI initialNs,
                                                                   final NormalizedNode<?, ?> inputStructure)
            throws IOException {
        final NormalizedNodeStreamWriter jsonStream = JSONNormalizedNodeStreamWriter.
                createExclusiveWriter(jsonCodecFactory, SchemaPath.ROOT, initialNs,
                        JsonWriterFactory.createJsonWriter(writer, 2));
        final NormalizedNodeWriter nodeWriter = NormalizedNodeWriter.forStreamWriter(jsonStream);
        nodeWriter.write(inputStructure);

        nodeWriter.close();
        return writer.toString();
    }

    private String translateNormalNode2Json(NormalizedNode node) throws IOException, URISyntaxException {
        final Writer writer = new StringWriter();
        URI initialNs = node.getNodeType().getModule().getNamespace();
        final String jsonOutput = normalizedNodeToJsonStreamTransformation(writer, initialNs, node);
        return jsonOutput;
    }

    public String genenrateJsonString(byte[] nodeBytes) throws IOException, URISyntaxException {
        NormalizedNode normalizedNode = SerializationUtils.deserializeNormalizedNode(nodeBytes);
        return translateNormalNode2Json(normalizedNode);
    }

    public <T extends DataObject> String genenrateJsonString(InstanceIdentifier<T> path, T data)
            throws IOException, URISyntaxException{
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.toNormalizedNode(path, data);
        return translateNormalNode2Json(normalizedNode.getValue());
    }

    public <T extends DataObject> byte[] generateNormalizedNodeBytes(InstanceIdentifier<T> path, T data) {
        Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> normalizedNode = codec.toNormalizedNode(path, data);
        return SerializationUtils.serializeNormalizedNode(normalizedNode.getValue());
    }

    public Map.Entry<InstanceIdentifier<?>, DataObject> generateDataObject(InstanceIdentifier nodeIdent, byte[] nodeBytes) {
        NormalizedNode normalizedNode = SerializationUtils.deserializeNormalizedNode(nodeBytes);
        YangInstanceIdentifier yangIid = codec.toYangInstanceIdentifier(nodeIdent);
        return generateDataObject(yangIid, normalizedNode);
    }

    public Map.Entry<InstanceIdentifier<?>, DataObject> generateDataObject(YangInstanceIdentifier yangIid,
                                                                           NormalizedNode normalizedNode) {;
        return codec.fromNormalizedNode(yangIid, normalizedNode);
    }

    @Override
    public void onGlobalContextUpdated(SchemaContext context) {
        this.schemaContext = context;
    }

    @Override
    public void close() throws Exception {
        if(contextListener != null)
            contextListener.close();;
    }

    public <T extends DataObject> Map.Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> generateNormalizedNode(InstanceIdentifier<T> path, T data) {
        return codec.toNormalizedNode(path, data);
    }
}
