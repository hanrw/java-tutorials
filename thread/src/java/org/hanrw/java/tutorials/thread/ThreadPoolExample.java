package org.hanrw.java.tutorials.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 阿里巴巴手册关于线程池的创建
 *
 * 强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，这 样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。
 * 说明：Executors 返回的线程池对象的弊端如下：
 * 1） FixedThreadPool 和 SingleThreadPool： 允许的请求队列长度为
 * Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM。
 * 2） CachedThreadPool： 允许的创建线程数量为
 * Integer.MAX_VALUE，可能会创建大量的线程，从而导致 OOM。
 *
 *
 *      队列                    最大创建线程数                                最大任务数                                                                 可以缓存个数
 * SynchrousQueue             maximumPoolSize                 (corePoolSize+创建的线程数) <=maximumPoolSize                                            无
 *
 * ArrayBlockingQueue         maximumPoolSize                 (corePoolSize+创建的线程数)(<=maximumPoolSize)+ArrayBlockingQueue设置的缓存数      ArrayBlockingQueue设置的缓存数
 *
 * LinkedBlockingQueue        corePoolSize                    corePoolSize＋LinkedBlockingQueue缓存的无界任务                                          无界
 *
 * @author hanrw
 * @date 2020/4/23 4:37 PM
 */
public class ThreadPoolExample {
  /**
   * Creates a new {@code ThreadPoolExecutor} with the given initial parameters.
   *
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set corePoolSize核心线程池大小,不会被销毁
   * @param maximumPoolSize the maximum number of threads to allow in the pool maximumPoolSize
   *     最大线程池数量,如果核心线程和阻塞队列容量都满了，切最大线程数大于核心线程数，可以再开启临时线程来处理
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating. keepAliveTime
   *     超时时间,如果没有新任务分配到线程的话,这个线程会被终止,如果线程数量大于核心线程数
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method. * workQueue
   *     阻塞队列
   *     如果运行线程数目大于核心线程数目时，也会尝试把新加入的线程放到一个BlockingQueue中去
   * @param threadFactory the factory to use when the executor creates a new thread threadFactory
   *     线程工厂
   * @param handler the handler to use when execution is blocked because the thread bounds and queue
   *     capacities are reached handler 拒绝策略如果线程和队列容量都满了的话
   * @throws IllegalArgumentException if one of the following holds:<br>
   *     {@code corePoolSize < 0}<br>
   *     {@code keepAliveTime < 0}<br>
   *     {@code maximumPoolSize <= 0}<br>
   *     {@code maximumPoolSize < corePoolSize}
   * @throws NullPointerException if {@code workQueue} or {@code threadFactory} or {@code handler}
   *     is null
   */

  /**
   * SynchronousQueue没有容量，是无缓冲等待队列。不会保存提交任务，超出直接corePoolSize个任务，直接创建新的线程来执行任务，直到(corePoolSize＋新建线程)> maximumPoolSize。不是核心线程就是新建线程。
   * 使用SynchronousQueue阻塞队列一般要求maximumPoolSizes为无界(Integer.MAX_VALUE)，避免线程拒绝执行操作
   */
  private static ExecutorService SynchronousCachedThreadPool =
      new ThreadPoolExecutor(
          4,
          Runtime.getRuntime().availableProcessors() * 2,
          0,
          TimeUnit.MILLISECONDS,
          new SynchronousQueue<>(),
          r -> new Thread(r, "ThreadTest"));

  /**
   * LinkedBlockingQueue是一个无界缓存等待队列。基于链表的先进先出，无界队列(如果不设置默认大小的话)。超出直接corePoolSize个任务，则加入到该队列中，直到资源耗尽,所以maximumPoolSize不起作用
   * 核心线程使用完毕,从队列里面取新任务
   *
   * Integer.MAX_VALUE，且在不指定队列大小的情况下也会默认队列大小为 Integer.MAX_VALUE
   */
  private static ExecutorService linkedCachedThreadPool =
      new ThreadPoolExecutor(
          4,
          Runtime.getRuntime().availableProcessors() * 2,
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(Integer.MAX_VALUE),
          r -> new Thread(r, "ThreadTest"));

  /**
   * ArrayBlockingQueue是一个有界缓存等待队列,创建时必须指定大小，超出直接corePoolSize个任务，则加入到该队列中，只能加该queue设置的大小，其余的任务则创建线程，直到(corePoolSize＋新建线程)> maximumPoolSize
   * 创建时必须指定大小
   *
   * 核心线程使用完,加到队列里面
   * 如果队列已满,核心线程数小于最大线程数
   * 则开启新线程进行处理
   *
   */
  private static ExecutorService arrayExecutorService =
      new ThreadPoolExecutor(
          4,
          Runtime.getRuntime().availableProcessors() * 2,
          0,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(Integer.MAX_VALUE),
          r -> new Thread(r, "ThreadTest"));

  /**
   * 四种拒绝策略
   */
  public  void allRejectedHandlers (){
    RejectedExecutionHandler rejected = null;
    rejected = new ThreadPoolExecutor.AbortPolicy();//默认，队列满了丢任务抛出异常
    rejected = new ThreadPoolExecutor.DiscardPolicy();//队列满了丢任务不异常
    rejected = new ThreadPoolExecutor.DiscardOldestPolicy();//将最早进入队列的任务删，之后再尝试加入队列
    rejected = new ThreadPoolExecutor.CallerRunsPolicy();//如果添加到线程池失败，那么主线程会自己去执行该任务
  }


  /**
   * 4种线程池：
   */
  public  void allThreadPool (){
    ExecutorService threadPool = null;
    threadPool = Executors.newCachedThreadPool();//SynchronousQueue队列,有缓冲的线程池，线程数 JVM 控制
    threadPool = Executors.newFixedThreadPool(3);//LinkedBlockingQueue队列,固定大小的线程池
    threadPool = Executors.newScheduledThreadPool(2);//DelayedWorkQueue队列 定时线程池
    threadPool = Executors.newSingleThreadExecutor();//LinkedBlockingQueue队列 单线程的线程池，只有一个线程在工作
  }


}
