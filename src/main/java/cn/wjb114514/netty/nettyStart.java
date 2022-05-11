package cn.wjb114514.netty;

/**
 * netty模型：
 *                  BossGroup只负责监听连接请求                                                 WorkerGroup
 *                       _________________                                                   ________________
 *                      | BossGroup                                                         |
 *                      | Selector                                                          |
 *                      |                                                                   |
 *        C1 C2 C3 -->  | Acceptor  ------->SocketChannel --> NIOSocketChannel -register->  | Selector ---> Handler
 *                      |__________________                                                 |____________________
 *  1.BossGroup线程维护一个Selector，只关注Accept事件
 *  2.当监听到了Accept事件，获取对应的socketChannel(获取客户端的socketChannel对象)，封装为NIOSocketChannel并注册到Worker线程（此线程就是一个事件循环线程，循环阻塞监听事件发生）
 *  3.当Worker线程监听到了 WorkerSelector中的通道[即由BossSelector注册到Worker线程的NIOSocketChannel客户端通道]发生了自己感兴趣[即当前Worker监听的事件类型]的事件后，进行处理
 *
 *
 *  详细版：
 *  1.netty抽象出两组线程池
 *  BossGroup{NioEventLoop{Selector TaskQueue}} BossGroup专门负责接收客户端的连接
 *  WorkerGroup{NioEventLoop{Selector TaskQueue}} WorkerGroup专门负责网络的读写
 *  2.BossGroup WorkerGroup的类型都是NioEventLoopGroup
 *  3.NioEventLoopGroup:事件循环组。这个组中含有多个事件循环都是一个NioEventLoop
 *  4.NioEventLoop 是一个不断循环执行 处理任务的线程。每个NioEventLoop都有一个selector，用于监听 [绑定]在其上的socket的网络通讯。
 *  服务器端在accept事件中，会获取客户端的socketChannel对象，并注册到workerGroup的selector上，因此workerGroup的selector就可以监听注册到其上 所有客户端socketChannel的事件
 *  5.NioEventLoopGroup可以有多个线程，即可以含有多个NioEventLoop
 *  6.每个BossNioEventLoop的执行步骤
 *  {
 *      1.轮询accept事件
 *      2.处理accept事件，与client建立连接，获取client端的socketChannel，封装为NioSocketChannel，并将其注册到某个workerGroup下的selector
 *      3.处理任务队列的任务，即runAllTasks
 *  }
 *  7.每个WorkerNioEventLoop循环执行的步骤
 *  {
 *      1.轮询read/write事件
 *      2.处理i/o事件，即read/write事件，在对应的nioSocketChannel处理，即监听到哪个Client，就处理哪个
 *      3.处理任务队列的任务，即runAllTasks。
 *  }
 *  8.存在pipeline管道，进行WorkerNioEventLoop的业务处理，此pipeline有很多处理器handler和Channel，即通过pipeline可以获取相应的channel。
 *  管道中维护很多处理器，进行数据的过滤等操作！
 */
public class nettyStart {
}
