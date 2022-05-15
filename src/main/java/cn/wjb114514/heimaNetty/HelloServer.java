package cn.wjb114514.heimaNetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 细节：
 * 1.ChannelInitializer<NioSocketChannel>() 这个初始化器，是服务器端的一个handler处理器
 * 我们在启动器里，只是注册了这么一个handler，但是不会立即触发。顾名思义：只有在连接建立后，才会触发
 * 2.我们使用group()后，就相当于boss线程启动了一个selector [只针对 == accept() 事件==循环，轮询，]
 * 2.5 我们使用channel()后，相当于选择一个具体的子类用于 serverSocketChannel。
 * 3.我们调用bind()后，服务器正式启动，并启动一个线程用来监听selector事件。
 *
 * ---------- 客户端来连接了 ----------
 * netty底层帮我们实现了 针对accept()请求的处理器。
 * 当有服务器连接时，服务器的selector监听到 【accept事件】
 * 并调用对于accept事件的处理器[netty实现好了]
 * 这个处理器调用 我们ChannelInitializer<NioSocketChannel>()初始化器的 ==> initChannel()方法
 * 同时，客户端在链接建立后，也会调用自己的初始化器。
 *
 * 初始化器的功能就是 ==> 在自己的channel上注册了 处理器[ch.pipeline().addLast(handler)]
 * 此handler可以对 channel上发生的事件，进行处理。
 *
 * .sync() 我们手动使用这个方法，进行阻塞，阻塞的解除 发生在 连接建立后
 * .channel() 我们通过此方法，可以获取要操作的channel，即连接对象
 * .writeAndFlush() 把数据写入缓冲区，并清空缓冲区 客户端 ===> 发送数据
 * ！！所有事件都会经过handler
 * hello,world == StringEncoder[ClientChannelHandler] ==> ByteBuf == StringDecoder[ServerChannelHandler] ==> String
 * == TestRedis[ChannelInBoundHandlerAdapter] ==> 此handler的处理方法就是，根据事件类型，调用相关方法[channelRead()]进行处理
 *
 * 可以看到，一个字符串 是通过管道在 客户端和服务器流动的
 *
 * 客户端数据 ==[socketChannel]==> ==客户端pipeline==> ==服务器端pipeline==> 服务器端[serverSocketChannel]
 * 其中，数据"hello,world" 经过客户端pipeline时，被注册到客户端上的处理器StringEncoder 变成 byteBuf["hello,world"]
 * 之后数据 byteBuf["hello,world"] 经过服务器端pipeline时，被服务器端处理器StringDecoder 变成 "hello,world"
 * 此时channel变为可读，之后继续经过pipeline的另一个处理器
 * 之后再经过处理器ChannelInBoundHandlerAdapter 触发其channelRead()方法
 * 最后到达服务器serverSocketChannel
 *
 */
public class HelloServer {
    public static void main(String[] args) {
        // 1.服务器端启动器 ： 用于把netty的组件组装，构成服务器
        new ServerBootstrap()
                // 2.BossEventLoop,WorkerEventLoop
                // EventLoop: 类似我们之前的boss/worker工作组， 内置了一个线程对象和 一个selector
                // Loop:就是我们在run方法里 while(true) 让selector一直循环
                // Event:就是我们在循环体里 对事件的监听。
                .group(new NioEventLoopGroup())
                // 3.channel:指定一个serverSocketChannel的具体实现子类。
                // NIO/BIO（OIO）/epoll/mapped(kQueue) 这些通道，都可以作为服务器serverSocketChannel的实现
                .channel(NioServerSocketChannel.class)
                // 4.childHandler 处理事件的处理器。也就是我们之前模拟的worker线程，用于处理读写事件
                // childHandler有很多种，可以负责编解码等...
                .childHandler(
                        // 5.此通道表示和客户端听到进行读写的通道[NioSocketChannel]
                        // 我们注册一个处理器handler[ChannelInitializer]
                        // 用于初始化客户端通道。
                       new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 6.在客户端通道添加具体的handler
                        // StringDecoder: 用于字符和字节流的 解码 [将ByteBuf转换为String]
                        ch.pipeline().addLast(new StringDecoder());
                        // 此ChannelInboundHandlerAdapter内部实现了我们自定义对 读写操作的业务逻辑处理。
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                // 7.把serverSocketChannel绑定到 主机的xx端口监听。
                .bind(1145);
    }
}
