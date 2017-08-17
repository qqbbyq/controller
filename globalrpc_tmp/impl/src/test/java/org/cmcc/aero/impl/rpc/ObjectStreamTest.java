package org.cmcc.aero.impl.rpc;

import java.io.*;

/**
 * Created by zhuyuqing on 2017/8/16.
 */

public class ObjectStreamTest {

  public static class Car {

    String name;

    public Car(String name){
      this.name = name;
    }
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    try {
      Car s = new ObjectStreamTest.Car("genius");
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(s);
      oos.close();
      bos.close();
      byte[] array = bos.toByteArray();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array));
      Car s1 = (Car) ois.readObject();
      ois.close();
      System.out.println(s1);

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }

  }
}
