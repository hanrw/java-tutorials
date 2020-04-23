package org.hanrw.java.basic.string;

/**
 * @author hanrw
 * @date 2020/4/23 6:21 PM
 */
public class StringExample {
  /** 问:创建几个对象 */
  public static String stringAPlusB = new String("A" + "B");
  // javap -verbose StringExample | grep  "= String"
  // Warning: Binary file StringExample contains org.hanrw.java.basic.string.StringExample
  //   #3 = String             #21            // AB

  // public static String stringAPlusB = new String("A" + "B")+"ABC";
  //  javap -verbose StringExample | grep  "= String"
  // Warning: Binary file StringExample contains org.hanrw.java.basic.string.StringExample
  //   #5 = String             #27            // AB
  //   #8 = String             #30            // ABC
  //
  //  public static String stringA = "A";
  //  public static String stringB = "B";
  //  public static String stringAB = "AB";
  //  public static String stringC = (stringA + stringB);

  public static void main(String[] args) {
    //    System.out.println(stringAPlusB == stringAB);
    //    System.out.println(stringC == stringAB);
  }
}
