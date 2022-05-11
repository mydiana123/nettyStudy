package cn.wjb114514.heimaNetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

/*
所谓 debug中的 evaluate expression
就是让当前阻塞的线程 活一下，然后执行这个表达式。之后继续阻塞在断点处
而debug的断点[我们选择Thread模式] 会导致只有执行这条语句的线程阻塞，而其他线程不阻塞。
而debug的断点[我们选择All模式] 此程序涉及的所有线程都阻塞，不止包括当前执行语句的线程
 */
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        // 1.客户端启动器，组装组件用于组成客户端: 返回值就是
        Channel channel = new Bootstrap()
                // 2.添加组件，给客户端注册一个 EventLoop组件[当然客户端也可以有一个selector用于监听服务器channel 的事件]
                .group(new NioEventLoopGroup())
                // 3.设置客户端socketChannel的具体实现子类
                .channel(NioSocketChannel.class)
                // 4.添加处理器，处理相关事件 ChannelInitializer这个处理器 用于初始化socketChannel
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    // 5.连接建立后，调用初始化器的initChannel方法，对channel初始化
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 6.给channel注册一个编码器[和服务器端的操作对称]
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 7.连接到服务器
                .connect("localhost", 1145)
                .sync()
                // 获取用于通讯的客户端socketChannel
                .channel();
        // 注意：此处的断点类型要设置为 Thread：即只会阻塞当前线程
        // 如果 我们选择了All，会把所有线程都阻塞。
        // 而我们用evaluate expression时 [channel.writeAndFlush()]
        // 真正操作channel发数据的线程，其实是NioEventLoop这个线程，而不是主线程
        // 客户端 [主线程] [NioEventLoop线程]==> 这个线程才是来发数据的
        // 但是我们使用All，把NioEventLoop也给阻塞了，这就导致主线程不动，NioEventLoop线程不动
        // 我们的目的就是让主线程不动，那NioEventLoop就动不了，writeAndFlush()方法就被卡住...

        // 为什么之前的NIO没有这问题呢? 我们知道 sc.write()执行这个方法的就是客户端主线程。之后selector对应一个轮询线程，服务器有一个处理线程
        // 所以我们把线程阻塞后，再执行表达式，相当于让主线程 解除阻塞一下下，先执行下我的表达式，然后继续在断点处阻塞。
        // 这样，执行表达式会导致main线程暂时不阻塞。
        // 但是netty里，执行表达式会导致 主线程分配一个worker线程来执行这个表达式，但是主线程还在那卡着呢，上哪给你分配
        // 因此采取Thread模式，即只会阻塞执行断点方法的线程，这样我们主线程继续工作，执行一个表达式，主线程发现需要一个worker
        // 就分配一个worker，然后worker停止阻塞，进行channel.writeAndFlush() 之后继续阻塞。

        // 执行这句话的线程是client的main线程的一个NioEventLoop线程[我们称之为worker]
        // 我们如果选择Thread，就只会阻塞worker。如果选择All，则会阻塞worker和main
        channel
                .writeAndFlush("hello,world"); // 写数据
    }
}
