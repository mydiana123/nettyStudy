package cn.wjb114514.heimaNetty;

/**
 * netty：异步的，基于事件驱动的，网络应用框架 [底层采取多路复用技术处理]
 * 注意，这里的异步 不指AIO。而是指netty应用多线程，把调用方法的线程 和处理结果的线程相分离
 * 用于快速开发 可维护 的。高性能的服务器和客户端
 *
 * Mina 和 netty 都由Trustin Lee进行开发
 *
 * 低位： netty在网络编程的低位 == spring在javaEE的低位
 * Cassandra ： 分布式no-sql
 * Hadoop：大数据分布式存储框架
 * Spark：大数据分布式计算框架
 * RocketMQ：阿里开源的消息队列
 * ES 全文搜索引擎
 * gRPC：rpc框架
 * Dubbo：rpc框架
 * Spring-5.0x 的响应式编程flux框架
 * ZooKeeper：分布式协调框架、
 *
 * netty：基于nio的包装。
 * 纯NIO开发：
 * 1.自己构建协议
 * 2.解决tcp传输的问题：黏包和半包
 * 3.epoll[Linux底层多路复用]空轮训导致CPU 100%占用。
 * NIO作者处理Linux的epoll时，可能导致selector不会阻塞，线程空轮询。
 * 4.增强API：使之更加强悍 ByteBuf => ByteBuffer FastThreadLocal => ThreadLocal
 *
 * netty = vs = 其他框架
 *
 */

/*
正确观念：
channel：数据的通道 通道内部有一个管道pipeline[加工流水线] 管道内部有很多handler[工序]
数据经过某channel时，会经过内部的pipeline，然后被里面的很多handler进行加工 然后把加工后的数据 发送出去。
其中，pipeline负责发布事件，而handler负责处理事件。
pipeline会导致channel的 事件类型发生变化，进而通过事件传递，流水线上的handler对自己感兴趣的通道状态[事件类型] 进行处理。

InBound：入站 ==> 数据输入时，会被入站处理器处理
OutBound：出站 ==> 数据输出时，会被出站处理器处理

EventLoop 处理数据的工人
线程 + selector
handler的工序 就是 由eventLoop里的工作线程 进行执行的。
1.EventLoop可以管理多个channel的IO操作 [selector可以监听多个channel ==> 多路复用]
netty：channel和EventLoop进行绑定 ==> channel上的所有读写事件，都由一个工人[EventLoop里的线程]处理
绑定目的：线程安全。 两个线程都执行了 工序[handler方法]。导致同一个数据被处理两边，造成一些问题
相当于一个工人 专一性的负责多个channel
2.工人既可以进行IO操作，也可以进行任务处理[比如之前的对字符串和ByteBuf的互转操作]，每个工人 都有自己的任务队列，队列里可以 堆放多个channel待处理任务，任务分为普通任务，定时任务
3.工人按照pipeline顺序，依次按照handler的规划处理数据，可以为每道工序[非IO操作] 指定不同的工人
 */
public class Start {
}
