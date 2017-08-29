package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import akka.actor.ActorSystem;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

/**
 * Created by zhuyuqing on 2017/8/21.
 */

public class SerializerTest {
  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("globalRpcSystem");

// Get the Serialization Extension
    Serialization serialization = SerializationExtension.get(system);

// Have something to serialize
//    String original = "woohoo";

// Find the Serializer for it
    Serializer serializer =
      serialization.serializerFor(org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder.ImmutableContainerNode.class);

// Turn it into bytes
//    byte[] bytes = serializer.toBinary(original);

// Turn it back into an object,
// the nulls are for the class manifest and for the classloader
//    String back = (String) serializer.fromBinary(bytes);

// Voilá!
//    assertEquals(original, back);
    System.out.println(serializer);
//    System.out.println(NormalizedNode.class.isAssignableFrom(org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder.ImmutableContainerNode.class));
  }


}
