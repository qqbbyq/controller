package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import akka.actor.ActorSystem;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import org.opendaylight.controller.cluster.datastore.node.utils.serialization.NormalizedNodeSerializer;
import org.opendaylight.controller.cluster.datastore.util.InstanceIdentifierUtils;
import org.opendaylight.controller.cluster.datastore.utils.SerializationUtils;
import org.opendaylight.controller.protobuff.messages.common.NormalizedNodeMessages;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

import java.io.*;
import java.net.URI;

/**
 * Created by zhuyuqing on 2017/8/21.
 */

public class SerializerTest {
  protected static final QNameModule TEST_MODULE = QNameModule.create(URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);

  public static void main(String[] args) throws IOException, ClassNotFoundException {
//    final YangInstanceIdentifier para = YangInstanceIdentifier.create(
//      new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, "lists")),
//      new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, "unordered-container"))
//    );


    NormalizedNode<?, ?> para = TestModel.createTestContainer();

    System.out.println("*************************8");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);

    int len = 0;
    System.out.println(para);
    if(para instanceof YangInstanceIdentifier) {
      System.out.println("generateTaskHead para" + para.getClass());
      YangInstanceIdentifier ins = ((YangInstanceIdentifier) para);
      NormalizedNodeMessages.InstanceIdentifier i = InstanceIdentifierUtils.toSerializable(ins);

//      for (byte b: bos.toByteArray()) {
//        System.out.print(b);
//      }
//      System.out.println();
//      System.out.println("bos.tobytearray before");
//
      oos.writeObject(i.toByteArray());
//      oos.writeObject(ins);

//      for (byte b: i.toByteArray()) {
//        System.out.print(b);
//      }
//      System.out.println();
//      System.out.println("write i.tobytearray");
    } else if(para instanceof NormalizedNode){

      NormalizedNodeMessages.Node expected = NormalizedNodeSerializer
        .serialize(para);

      for (byte b: bos.toByteArray()) {
        System.out.print(b);
      }
      System.out.println();
      System.out.println("bos.tobytearray before");
//
      oos.writeObject(expected.toByteArray());
//      oos.writeObject(ins);

      for (byte b: expected.toByteArray()) {
        System.out.print(b);
      }
      System.out.println();
      System.out.println("write i.tobytearray");
//
    } else {
      //      oos.writeObject(para);

    }
    System.out.println();

    byte[] r = bos.toByteArray();
    for (byte b: bos.toByteArray()) {
      System.out.print(b);
    }
    System.out.println();
    System.out.println("len=" + r.length);

    ByteArrayInputStream bis = new ByteArrayInputStream(r);
    ObjectInputStream ois = new ObjectInputStream(bis);
//    YangInstanceIdentifier tmp = (YangInstanceIdentifier) ois.readObject();
//    System.out.println(tmp);
//    System.out.println(tmp.getClass());
    byte[] tmp = (byte[]) ois.readObject();
//    for (byte b: tmp) {
//      System.out.print(b);
//    }
//    System.out.println("read ");

//    NormalizedNodeMessages.InstanceIdentifier p = NormalizedNodeMessages.InstanceIdentifier.parseFrom(tmp);
    NormalizedNodeMessages.Node p = NormalizedNodeMessages.Node.parseFrom(tmp);
    System.out.println(p);

//    Object r2 = InstanceIdentifierUtils.fromSerializable(p);
    Object r2 = NormalizedNodeSerializer.deSerialize(p);

    System.out.println(r2);
    System.out.println(r2.getClass());

















    /*
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
  */
  }


}
