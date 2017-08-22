package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableNormalizedValueAttrNode;

/**
 * Created by zhuyuqing on 2017/8/21.
 */

public class SerializerTest {
  public static void main(String[] args) {
    System.out.println(NormalizedNode.class.isAssignableFrom(AbstractImmutableNormalizedValueAttrNode.class));
  }
}
