package cn.wjb114514.netty.Code;

import io.netty.util.NettyRuntime;

/**
 * 1. 线程组 NioEventLoopGroup含有的默认线程数为 CPU核数 * 2
 *
 * 此后，bossGroup的children属性就含有24个NioEventLoop对象（我们的电脑有12个核） 编译类型：EventExecutor
 *
 * public NioEventLoopGroup() {
 *     this(0);
 * }
 * public NioEventLoopGroup(int nThreads) {
 *     this(nThreads, (Executor) null);
 * }
 * public NioEventLoopGroup(int nThreads, Executor executor) {
 *     this(nThreads, executor, SelectorProvider.provider());
 * }
 * public NioEventLoopGroup(
 *         int nThreads, Executor executor, final SelectorProvider selectorProvider) {
 *     this(nThreads, executor, selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
 * }
 * public NioEventLoopGroup(int nThreads, Executor executor, final SelectorProvider selectorProvider,
 *                          final SelectStrategyFactory selectStrategyFactory) {
 *     super(nThreads, executor, selectorProvider, selectStrategyFactory, RejectedExecutionHandlers.reject());
 * }
 * protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
 *     super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
 * }
 * private static final int DEFAULT_EVENT_LOOP_THREADS;
 *
 * static {
 *     DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
 *             "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2)); //nettyRuntime.avilableProcessors:返回本机可用的cpu核数
 * public static void main(String[] args) {
 *     System.out.println(NettyRuntime.availableProcessors()); // 返回12 说明我们的电脑是12核心的CPU 2CPU 六核
 * }
 * 服务器读取线程：nioEventLoopGroup-3-1
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x984105ac, L:/127.0.0.1:6668 - R:/127.0.0.1:60690])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-2
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x27769a95, L:/127.0.0.1:6668 - R:/127.0.0.1:60744])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-3
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x9afd3390, L:/127.0.0.1:6668 - R:/127.0.0.1:60800])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-4
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x2fa8185f, L:/127.0.0.1:6668 - R:/127.0.0.1:60854])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-5
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x81185c01, L:/127.0.0.1:6668 - R:/127.0.0.1:60907])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-6
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x04875db5, L:/127.0.0.1:6668 - R:/127.0.0.1:60960])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-7
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0x28c5dc98, L:/127.0.0.1:6668 - R:/127.0.0.1:61014])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-8
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0xad7513dc, L:/127.0.0.1:6668 - R:/127.0.0.1:61066])
 * 客户端发送数据是hello,server：猫喵喵~
 * 服务器读取线程：nioEventLoopGroup-3-1
 * server ctx =ChannelHandlerContext(ServerHandler#0, [id: 0xccae1f49, L:/127.0.0.1:6668 - R:/127.0.0.1:61119])
 * 客户端发送数据是hello,server：猫喵喵~
 * 我们把WorkerGroup分配8个子线程，如果有多个客户端，就会循环使用 1 2 3 4  5 6 7 8  1 2 3 4 5 6 7 8 ...
 * bossGroup下的线程对象NioEventLoop含有多个对象：比如selector，taskQueue。同理workerGroup下的每一个线程都有自己的selector。
 * Channel Pipeline ctx的关系：把 断点下在 服务器端监听客户端读事件的地方，开辟一个客户端，，进行debug。 于是在服务器的channelRead方法处进行了截获
 * ctx[编译类型为DefaultChannelHandlerContext]包含的内容： handler[nettyServerHandler],next,prev[双向链表],inbound,outbound[入站or出站],pipeline{head,tail 本质是双向链表，channel[通过pipeline可以拿到channel{channel里面还有pipeline}]},eventLoop[ctx知道他在哪个worker线程] [channel <-> pipeline是互相包含的，是互相对应的。]
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(NettyRuntime.availableProcessors());
    }
}
