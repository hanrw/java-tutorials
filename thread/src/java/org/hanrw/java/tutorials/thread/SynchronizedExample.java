package org.hanrw.java.tutorials.thread;

/**
 * @author hanrw
 * @date 2020/3/23 8:52 PM
 *
 */


public class SynchronizedExample {

  private volatile String sharedData;



//  // access flags 0x21
//  public synchronized synchronizedOnMethod()V
//  L0
//  LINENUMBER 12 L0
//  ALOAD 0
//  LDC "synchronizedOnMethod"
//  PUTFIELD org/hanrw/concurrency/synchronize/ExplainSynchronized.sharedData : Ljava/lang/String;
//  L1
//  LINENUMBER 13 L1
//                    RETURN
//  L2
//  LOCALVARIABLE this Lorg/hanrw/concurrency/synchronize/ExplainSynchronized; L0 L2 0
//  MAXSTACK = 2
//  MAXLOCALS = 1
// ====================================================javap -v ExplainSynchronized.class
//public synchronized void synchronizedOnMethod();
//  descriptor: ()V
//  flags: ACC_PUBLIC, ACC_SYNCHRONIZED
//  Code:
//  stack=2, locals=1, args_size=1
//                                  0: aload_0
//         1: ldc           #2                  // String synchronizedOnMethod
//                              3: putfield      #3                  // Field sharedData:Ljava/lang/String;
//                                                   6: return
//  LineNumberTable:
//  line 12: 0
//  line 13: 6
//  LocalVariableTable:
//  Start  Length  Slot  Name   Signature
//            0       7     0  this   Lorg/hanrw/concurrency/synchronize/ExplainSynchronized;

  public synchronized void synchronizedOnMethod() {
    sharedData = "synchronizedOnMethod";
  }

  // access flags 0x2A
//  private static synchronized synchronizedOnStaticMethod()V
//  L0
//  LINENUMBER 16 L0
//  GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
//  LDC "synchronizedStaticMethod"
//  INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
//                                                                    L1
//  LINENUMBER 17 L1
//                    RETURN
//  MAXSTACK = 2
//  MAXLOCALS = 0
  private static synchronized void synchronizedOnStaticMethod() {
    System.out.println("synchronizedStaticMethod");
  }



//  access flags 0x1
//  public synchronizedOnObject()V
//  TRYCATCHBLOCK L0 L1 L2 null
//  TRYCATCHBLOCK L2 L3 L2 null
//  L4
//  LINENUMBER 20 L4
//  ALOAD 0
//  DUP
//  ASTORE 1
//  MONITORENTER
//      L0
//  LINENUMBER 21 L0
//  ALOAD 0
//  LDC "synchronizedOnMethod"
//  PUTFIELD org/hanrw/concurrency/synchronize/ExplainSynchronized.sharedData : Ljava/lang/String;
//  L5
//  LINENUMBER 22 L5
//  ALOAD 1
//  MONITOREXIT
//      L1
//  GOTO L6
//  L2
//  FRAME FULL [org/hanrw/concurrency/synchronize/ExplainSynchronized java/lang/Object] [java/lang/Throwable]
//  ASTORE 2
//  ALOAD 1
//  MONITOREXIT
//      L3
//  ALOAD 2
//  ATHROW
//      L6
//  LINENUMBER 23 L6
//  FRAME CHOP 1
//  RETURN
//      L7
//  LOCALVARIABLE this Lorg/hanrw/concurrency/synchronize/ExplainSynchronized; L4 L7 0
//  MAXSTACK = 2
//  MAXLOCALS = 3

  public void synchronizedOnObject() {
    synchronized (this) {
      sharedData = "synchronizedOnMethod";
    }
  }

  // access flags 0x1
//public synchronizedOnClass()V
//           TRYCATCHBLOCK L0 L1 L2 null
//           TRYCATCHBLOCK L2 L3 L2 null
//           L4
//           LINENUMBER 26 L4
//           LDC Lorg/hanrw/concurrency/synchronize/ExplainSynchronized;.class
//DUP
//    ASTORE 1
//               MONITORENTER
//               L0
//               LINENUMBER 27 L0
//               GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
//               LDC "synchronizedStaticMethod"
//               INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
//               L5
//               LINENUMBER 28 L5
//               ALOAD 1
//               MONITOREXIT
//               L1
//               GOTO L6
//               L2
//               FRAME FULL [org/hanrw/concurrency/synchronize/ExplainSynchronized java/lang/Object] [java/lang/Throwable]
//               ASTORE 2
//               ALOAD 1
//               MONITOREXIT
//               L3
//               ALOAD 2
//               ATHROW
//               L6
//               LINENUMBER 29 L6
//               FRAME CHOP 1
//               RETURN
//               L7
//               LOCALVARIABLE this Lorg/hanrw/concurrency/synchronize/ExplainSynchronized; L4 L7 0
//               MAXSTACK = 2
//               MAXLOCALS = 3
// ====================================================javap -v ExplainSynchronized.class
//  public void synchronizedOnClass();
//  descriptor: ()V
//  flags: ACC_PUBLIC
//  Code:
//  stack=2, locals=3, args_size=1
//                                  0: ldc           #7                  // class org/hanrw/concurrency/synchronize/ExplainSynchronized
//                                                       2: dup
//         3: astore_1
//         4: monitorenter
//         5: getstatic     #4                  // Field java/lang/System.out:Ljava/io/PrintStream;
//                              8: ldc           #5                  // String synchronizedStaticMethod
//                                                   10: invokevirtual #6                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
//                                                                         13: aload_1
//        14: monitorexit
//        15: goto          23
//            18: astore_2
//        19: aload_1
//        20: monitorexit
//        21: aload_2
//        22: athrow
//        23: return

  public void synchronizedOnClass() {
    synchronized (SynchronizedExample.class) {
      System.out.println("synchronizedStaticMethod");
    }
  }
}
