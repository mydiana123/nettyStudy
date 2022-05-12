package cn.wjb114514.heimaNetty.Channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/*
只要是 处理xxxFuture xxxPromise的，都是异步方法.
 */
@Slf4j
public class PureClient2 {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup group = new NioEventLoopGroup();
        // 只要是xxxFuture/promise 都是和异步方法配套使用的，用于正确处理异步返回结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // netty提供了一个日志组件，可以在处理数据时自动调用。
                        /*
                        16:41:39.313 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x2a329b50] REGISTERED
16:41:39.314 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x2a329b50] CONNECT: localhost/127.0.0.1:1145
16:41:39.317 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x2a329b50, L:/127.0.0.1:60136 - R:localhost/127.0.0.1:1145] ACTIVE
16:41:39.317 [main] DEBUG cn.wjb114514.heimaNetty.Channel.PureClient2 - [id: 0x2a329b50, L:/127.0.0.1:60136 - R:localhost/127.0.0.1:1145]
16:41:39.350 [main] DEBUG cn.wjb114514.heimaNetty.Channel.PureClient2 - 处理关闭后的善后工作
亲~请问您想说什么呀
a
16:42:08.001 [input] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.maxCapacityPerThread: 4096
16:42:08.001 [input] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.ratio: 8
16:42:08.001 [input] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.chunkSize: 32
16:42:08.002 [input] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.blocking: false
亲~请问您想说什么呀
16:42:08.009 [nioEventLoopGroup-2-1] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkAccessible: true
16:42:08.009 [nioEventLoopGroup-2-1] DEBUG io.netty.buffer.AbstractByteBuf - -Dio.netty.buffer.checkBounds: true
16:42:08.010 [nioEventLoopGroup-2-1] DEBUG io.netty.util.ResourceLeakDetectorFactory - Loaded default ResourceLeakDetector: io.netty.util.ResourceLeakDetector@f38ae9b
16:42:08.013 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x2a329b50, L:/127.0.0.1:60136 - R:localhost/127.0.0.1:1145] WRITE: 1B
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 61                                              |a               |
+--------+-------------------------------------------------+----------------+
16:42:08.013 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x2a329b50, L:/127.0.0.1:60136 - R:localhost/127.0.0.1:1145] FLUSH

                         */
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                }).connect("localhost", 1145);
        Channel channel = channelFuture.sync().channel();

        log.debug("{}",channel);
        // 需求：开辟一个线程，可以源源不断的让客户端向服务器端发送数据，用户按q退出
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("亲~请问您想说什么呀");
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    /*
                    亲~请问您想说什么呀
q
16:44:01.777 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x5034fe88, L:/127.0.0.1:60393 - R:localhost/127.0.0.1:1145] CLOSE
16:44:01.777 [input] DEBUG cn.wjb114514.heimaNetty.Channel.PureClient2 - 处理关闭后的善后工作
16:44:01.778 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x5034fe88, L:/127.0.0.1:60393 ! R:localhost/127.0.0.1:1145] INACTIVE
16:44:01.778 [nioEventLoopGroup-2-1] DEBUG io.netty.handler.logging.LoggingHandler - [id: 0x5034fe88, L:/127.0.0.1:60393 ! R:localhost/127.0.0.1:1145] UNREGISTERED
我们发现，线程的关闭CLOSE[由异步线程NioEventLoop执行] 和log.debug这句话 的执行[由用户线程input执行]，的顺序，有可能先关闭 再处理善后工作。还有可能先善后，再关闭
而本质原因就是 close的异步性
                     */
                    channel.close();
                    // 这个方法也是异步方法，因此，close()的调用者main线程不会等到处理close()方法NioEventLoop线程的结果返回后，才继续执行。所以会立马执行下面的打印语句...
                    // 这句话 ==> 不会真的等我们把channel关闭后才打印。因为主线程在接收到正确的 异步线程结果后，就不阻塞了，因此就直接执行这句代码
                    log.debug("处理关闭后的善后工作");
                    break;
                }
                channel.writeAndFlush(line);
            }
        },"input").start();

        // 解决方法 ==> 使用closeFuture处理异步close 的结果
        ChannelFuture cf = channel.closeFuture();
        System.out.println("===模拟关闭===");
        // 此sync()方法会等待 执行close() 的异步线程 返回处理完毕的信号后，才解除阻塞
        // 所谓异步，就是指 调用方法的线程和 执行方法的线程不一样，也就是说 调用方法的线程 把执行任务交给了异步线程处理，之后等待异步线程的处理结果。
        // cf.sync();
        // System.out.println("===我是处理工作，一定会在close()后执行===");

        // 解决方法2：注册listener ==> 调用方法的线程 不会阻塞，继续进行，而异步线程执行方法后，会调用方法绑定的回调对象，回调对象完成异步线程的返回结果。
        // 可以使用函数式接口的 lambda表达式简化
        cf.addListener(new ChannelFutureListener() {
            @Override
            // 只有异步线程 执行的操作结束后[也就是close()方法结束后]，才会调用 此回调方法
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("处理关闭后的结果....");
                // 我们关闭连接后，客户端通道被关闭，那NioEventLoopGroup的线程还在运行，也应该被关闭
                group.shutdownGracefully();
            }
        });


    }
}



