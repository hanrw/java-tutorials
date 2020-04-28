# 线程池的重要参数以及工作流程
  - corePoolSize
    
    保存在线程池里面的核心线程
    
  - maximumPoolSize
    
    最大线程数
    
  - keepAliveTime
    
    超时时间,如果一个非核心线程没有在超时时间以内分配到任务,会销毁此线程
     
  - workQueue
   
    工作队列
      
      SynchronousQueue无缓冲队列,如果核心线程已经被全部使用,那么开启新线程执行
      
      LinkedBlockingQueue无界缓冲队列,超出直接corePoolSize个任务，则加入到该队列中
      
      ArrayBlockingQueue有界缓冲队列,超出直接corePoolSize个任务，则加入到该队列中，只能加该queue设置的大小，其余的任务则创建线程
   
  - handler
      
      四种拒绝策略
      
      AbortPolicy();//默认，队列满了丢任务抛出异常
      
      DiscardPolicy();//队列满了丢任务不异常
      
      DiscardOldestPolicy();//将最早进入队列的任务删，之后再尝试加入队列
      
      CallerRunsPolicy();//如果添加到线程池失败，那么主线程会自己去执行该任务


# Synchronized和lock的区别,以及AQS工作原理

  - Synchronized
     - 是JAVA内置的隐式加锁方式.不需要手动调用加锁和解锁
     - Synchronized可以在方法和对象上加锁
       - 在方法上加锁,会在字节码文件里面加一个flag标志位ACC_SYNCHRONIZED
       - 在对象上加锁,会在字节码文件里面添加MONITORENTER和MONITOREXIT
       - 最终都会使用ObjectMonitor对象进行加锁解锁
     - Synchronized扩展出
       - 逃逸分析(建立在JIT逃逸分析之上-通俗地讲,逃逸分析就是确定一个变量要放堆上还是栈上)
         - 是否有在其他地方（非局部）被引用。只要有可能被引用了，那么它一定分配到堆上。否则分配到栈上
         - 即使没有被外部引用，但对象过大，无法存放在栈区上。依然有可能分配到堆上
         - 在JIT编译阶段确立逃逸
       - 锁的消除是指,JIT编译阶段发现一个加锁方法只能被当前线程执行,也就是说加这个锁是没有必要的.JVM优化后,不需要加锁         
       - 锁的粗化是指,JIT编译阶段发现一个方法同时加了多次锁,JVM优化后认为可以在这段代码加一把锁就可以了
       - 锁的升级包括以下几个步骤(锁的状态是记录在对象头里面的)
         - 刚实例化好的一个对象是无锁状态
         - 一个线程开始执行到这个对象时,此时是偏向锁
         - 当第二个线程执行到这个对象时,发现这个对象已经有偏向锁了,那么锁升级到轻量级锁/自旋锁/无锁(CAS)
         - 当越来越多的线程执行到这个对象时,竞争就很激烈了,那么锁升级到重量级锁(系统底层互斥量实现)
   
  - Lock锁
     - ReentrantLock(可重入锁)是JAVA API提供的一种轻量级锁机制基于AQS
       - 公平锁
         - 如果获取锁失败,会判断队列是否已经有其他的线程占有.如果没有则设置成当前线程
       - 非公平锁
         - 获取锁时,会先去竞争修改资源.如果修改资源失败,进行排队CLH同步队列
       - 运行机制
         - 加入同步队列时,会去调用LockSupport.park(this)阻塞此线程
         - 一个线程执行完毕后,回去换新同步队列的第一个线程调用LockSupport.unpark(node.thread)唤醒线程
     



