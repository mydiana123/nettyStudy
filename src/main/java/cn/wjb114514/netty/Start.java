package cn.wjb114514.netty;

/**
 * 原本NIO的问题
 * 1.NIO的类库和API复杂，使用麻烦，需要熟练掌握三组件
 * 2.只有具备了java多线程，网络编程，才能写出高质量的程序，因为NIO涉及到Reactor模式
 * 3.开发工作量和难度都很大，例如客户端面临断连重连，网络闪断，半包读写，失败缓存，网络拥塞，以及异常流的处理。
 * 4.JDK的NIO本身存在bug，比如epoll bug，导致selector空轮询，CPU 100%
 *
 * netty：是一个异步的，基于事件驱动的网络框架，为了快速开发高性能的客户端和服务器端
 *
 * Core Extensible Event Model + Universal Communication API + ZeroCopy-Capable Rich Byte Buffer
 * Protocol Support : Http&WebSocket zlib/gzip SSL/TLS GoogleProtoBuf LargeFileTransfer
 * Transport Services : Socket&Datagram HttpTunnel In-Vm Pipe
 *
 * 知名的Dubbo和ES(ElasticSearch)框架内部就使用了netty
 *
 * 优点：
 * 1.设计优雅，适用于各种传输类型的阻塞/非阻塞的统一API
 * 2.灵活可拓展的事件模型
 * 3.一个或多个线程池
 * 4.详细记录的javaDoc，用户指南和实例，没有其他的依赖项
 * 5.更高的吞吐量，延迟更低，最小化不必要的内存copy
 * 6.支持多种协议和传输服务
 * 7.社区氛围好，版本更新快
 * 目前使用稳定的netty4.x版本
 *
 * netty的高性能架构设计
 * 目前存在的 线程模型
 * 传统阻塞的IO服务模型
 * Reactor模式： 1.单Reactor单线程，2.单Reactor多线程，3.主从Reactor多线程
 * netty基于主从Reactor模型进行了一定改进
 *
 * 多个Client向应用程序发起请求，应用程序为每个连接分配一个处理线程。
 * 处理线程的步骤：1.read(读取客户端的数据，这过程是阻塞的)，2.handler方法进行业务处理 3.send(把处理结果返回给客户端。阻塞)
 * 问题：
 * 1.并发数很大时，服务器需要创建多个线程，占用很多系统资源
 * 2.如果连接建立了，但是客户端没有数据可读，服务器线程就会一直阻塞，等待客户端的数据
 *
 * Reactor模式：
 * 解决方案：
 * 1.使用IO复用模型，多个连接共用一个阻塞对象，应用程序只需要在一个阻塞对象等待，当有连接有新的数据可以处理时，操作系统通知应用程序，进行处理
 * 2.基于线程池 复用线程资源。不必再为每个请求分配线程，线程 处理完之后，处于空闲状态，不会被释放，而是继续服务其他请求。
 * 反应器模式，分发者模式
 * Client1 Client2 Client3
 *     ServiceHandler(只提供一个阻塞对象)
 *      ThreadPool(Thread1 Thread2 Thread3) 在每个线程里，都有一个handler(handler有三个API，read，control，send。即读取数据，业务逻辑处理，返回结果)
 *      当有连接建立后，就会把具体的请求交给线程池里的Thread
 *
 * 传统IO架构相当于每个线程都是阻塞的，只要与线程关联的客户端没有发来数据，就等着。
 * 而Reactor模式中引入一个ServiceHandler用于分发事件Event
 * 也就是说，传统IO的工作模式是 读Client数据-->处理--->等待下一次的数据-->继续读Client数据-->处理-->继续等...
 * Reactor的工作模式是： Client有数据可以读-->分发给相应线程处理-->事件监听器继续阻塞监听，线程处理完了就干别的，不用等-->Client有数据可读-->分发给线程处理...
 * 这样：实际上阻塞的只有分发线程的那个Handler，而实际处理业务逻辑的线程，只有得知有IO事件时，才会进行读取，可以认为是IO不阻塞的。
 *
 * Reactor模式：通过一个或 多个数据 同时传递给服务器处理的模式
 * 服务器端程序处理传入的多个请求，并将他们同步分派到相应处理线程，因此Reactor模式也叫Dispatcher模式
 * 只有Reactor监听到事件时，才会交给线程处理，这样线程本身就不会阻塞，因为线程已经知道什么时候该读了，而不是傻等着下一次的读
 *
 * Reactor：事件的分发员
 * Handlers:事件的处理者。
 * 原来Handlers不知道什么时候会发生读写事件，因此采取傻等方式，阻塞监听
 * 现在Reactor来了，Handler会根据Reactor分发来的事件进行处理，这样Handler就不用等待了，换而Reactor会阻塞。
 *
 * 单Reactor，单线程： 我们的NIO代码就可以看成这个模型
 *                        C1      C2       C3
 *                           Reactor(select[阻塞监听事件的方法],dispatch[分发事件的方法]) -- 功能：阻塞监听事件，并分发到相应处理器
 *             Accept(accept[处理连接请求]) Handler(read,handle,send[进行IO操作和业务处理])
 * 缺点：仅有一个Reactor（监听线程），并且处理线程Handler以及Accept和Reactor共用一个线程，高并发时造成卡顿
 * 注意:这里的单线程指的是监听事件以及分发事件的线程是单线程的，甚至处理线程也是单线程的，三个人用一个线程
 * 说明：
 * 1.select()方法可以实现应用程序通过一个阻塞对象(NIO里的Selector)。
 * 总之一句话：谁监听谁阻塞，原来是处理线程 同时负责处理和监听，现在监听工作外包给别人干了。
 * 2.select()方法监听到事件后，会通过dispatch()方法分发到处理线程，免除了处理线程的阻塞监听
 * 3.如果事件类型为建立连接，会由Acceptor的accept()方法处理
 * 4.如果是业务请求，会调用Handler对象来处理
 * 5.Handler对象由 read->handle->send流程处理
 * 优点：模型简单，无多线程，进程通信，竞争的问题，全部集中在一个线程完成
 * 缺点：性能问题，无法充分调度系统资源，业务处理时，如果太耗时，别的人就要等。比如公司谈业务，只有一个老板在处理业务，这样的话业务A没处理完，老板也不能抽身去处理业务B
 * 可靠性问题：单点出问题，全体死亡。就一个线程，如果一个环节出问题导致线程崩了，其他的环节也要崩了
 * 使用场景：业务处理非常快速，客户端数量有限，比如Redis 时间复杂度O(1)时，可以使用
 *
 * 单Reactor多线程模式
 * 就是单Reactor单线程的基础上，引入了线程池。
 *                                              C1          C2          C3
 *                                                 Reactor(select,dispatch)
 *                                              Acceptor(accept)  Handler1  Handler2 Handler3(read,send)
 *                                                                       ThreadPool(WorkerThread1...n)[handle]
 * 工作原理：
 * 1.Reactor对象通过select监控客户端请求，收到事件时，进行分发
 * 2.如果是连接建立请求，Acceptor通过accept方法处理连接请求，并创建一个handler对象，完成连接完成后的相关操作。
 * 3.如果不是连接请求，reactor分发一个handler进行处理
 * 4.！现在的handler，只负责响应时间：即读取客户端的数据，并把结果返回给客户端，生成结果的过程， 也就是业务逻辑处理的handle方法，继续向下分发
 * 分发给线程池Worker的某个线程WorkerThread来处理
 * 5.workerThread完成真正的业务逻辑，并把处理结果返回给Handler
 * 6.Handler收到结果后，返回Client
 * 好处：把最费时的业务处理，交给线程池独立线程处理
 *
 * 优点：可以充分的利用多核CPU的处理能力(线程池)
 * 缺点：多线程数据共享和访问比较复杂。Reactor处理所有的事件请求[事件的监听和响应，在单线程运行，高并发容易出现瓶颈[因为Reactor要循环遍历每一个请求，相当于快递员一个人分发10000个快递，人都累趴了]]
 *
 * 主从Reactor多线程。
 *              C1          C2          C3
 *                     MainReactor      ---> Acceptor[accept]
 *                          ↓
 *              SubReactor1  SubReactor2   [select,dispatch]
 *                   ↓            ↓
 *               Handler[read,send]
 *                   ↓
 *              ThreadPool [handle]
 *
 *              可以看到：框架的本质就是分层，计算机的事，如果分一层不能解决， 就多分几层
 *              分层可以使得功能模块化，使得功能更加好调控，更强大
 * 1.Reactor主线程MainReactor对象通过select监听 [连接事件] 收到事件后，Acceptor进行处理
 * 2.Acceptor处理事件后，MainReactor将连接分配给SubReactor
 * 3.subReactor将MainReactor分配来的连接，加入[连接队列]并进行监听，并创建Handler对象进行各种事件处理
 * 4.当监听到连接队列里某连接的事件是，就会调用Handler进行处理
 * 5.Handler再把业务处理分发给线程池的工作线程，工作线程处理好后，把处理结果返回给handler
 * 6.handler收到响应结果后，在通过send返回给client
 *
 * 优点：分层明确，功能模块化，父子线程数据交互简单。（主reactor和子reactor之间的数据交互是单向的）
 * 缺点：变成复杂度高
 * nginx 主从多进程 memcached 主从多线程 netty 改进版主从多线程
 *
 * 单Reactor单线程：前台接待员和服务员同一个人
 * 单Reactor多线程：一个前台接待员，多个服务员
 * 主从：前台主管->多个前台接待员->多个服务员
 *
 * 优点：响应快，不必为单个同步时间阻塞，虽然reactor依然是同步的(即IO数据的操作必须等到IO操作完毕后才能进行，我们的非阻塞不过是把IO操作和IO数据的操作分离了)
 * 扩展性好：功能的模块化使得功能的增加方便
 * 复用性好：和具体事件无关，可以复用于各种事件
 */
public class Start {
}

/*
其实netty的本质 就是基于 事件-回调函数 的模型，也就是 事件驱动，把监听[循环阻塞操作]
监听其实是很无聊的，因为监听就像是侦察兵，侦查的时候必须啥也不干。
那么原本的io模型就是 所有人都在监听，都在阻塞，监听到了就处理，也就是监听-处理是一人兼顾的，想想也知道监听的时候不可以处理事情，多蛋疼。
而reactor模型把监听和处理的工作分离，监听负责监听事件，具体的处理工作（回调函数）由其他人完成
 */