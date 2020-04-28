package org.hanrw.java.tutorials.thread;

/**
 * @author hanrw
 * @date 2020/4/10 4:01 PM
 */
public class ThreadLocalExample {

  public static void main(String[] args) {
    /*
    实例化String thread local对象
     */
    ThreadLocal<String> stringThreadLocal = new ThreadLocal<>();

    /*
    线程T1
     */
    Thread thread1 =
        new Thread(
            () -> {
              stringThreadLocal.set("t1");
            });

    /*
    线程T2
     */
    Thread thread2 =
        new Thread(
            () -> {
              System.out.println("t2: is " + stringThreadLocal.get());
            });

    thread1.start();
    thread2.start();
  }


}
