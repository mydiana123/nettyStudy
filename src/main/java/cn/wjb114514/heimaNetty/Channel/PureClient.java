package cn.wjb114514.heimaNetty.Channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PureClient {
    public static void main(String[] args) throws InterruptedException {

        // 只要是xxxFuture/promise 都是和异步方法配套使用的，用于正确处理异步返回结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect("localhost", 1145);


        // 如果channelFuture不调用sync()方法，导致无法把数据发送到服务器?
        /*
        1.connect() ==> 异步非阻塞方法
        异步=>调用connect()方法的线程[main]，把结果处理交给了其他线程[NioEventLoop]。
        非阻塞:调用connect()方法的线程[main]不关心结果
        因此调用connect()方法的线程[main]继续进行
        2.建立连接需要时间，因此 [!从channelFuture对象里，获取channel需要一定时间，也就是说NioEventLoop需要一定事件后，才能把channel对象返回]
        而由于我们没有调用sync()方法，导致继续执行channel()方法，这里获取到的肯定不是 NioEventLoop返回的那个channel。
        因此无法和服务器进行通讯

        // ====channel信息====
        // 16:14:44.147 [main] DEBUG cn.wjb114514.heimaNetty.Channel.PureClient - [id: 0xd43f5581
        // 我们现在获取的channel对象，仅仅是一个channel对象，传统的channel对象的格式为 /ip:port
        // 也就是说，如果我们没等异步线程把channel返回完呢，就调用了获取channel的方法，就导致我们和这个channel的对话都是无用功
        // Channel channel = channelFuture.sync().channel();
        */
        /*
        // 1) 使用sync()方法同步处理结果 ==>
        // 调用异步方法的线程[main]阻塞，等到执行异步方法的线程[NioEventLoop]返回结果解除阻塞[应该是通过回调机制通知主线程结果已经返回]
        // Channel channel = channelFuture.sync().channel();
        // System.out.println("====channel信息====");
        // log.debug("{}",channel);
        //channel.writeAndFlush("hello,world");
         */

        /* 2)使用addListener()方法处理异步结果
        方法1 是主线程等待NioEventLoop线程的返回结果，等不到则阻塞
        阻塞的是主线程。
        addListener(回调对象) 会导致主线程也不会阻塞。
        因此主线程啥也不用干， 处理方法的线程 进行方法处理，并把返回值通过回调函数返回给主线程
        */
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                // 此方法在 异步线程处理完方法，获得结果后自动调用，是 为异步线程绑定的回调函数，用于处理异步线程返回结果。
                Channel channel = channelFuture.channel();
                // 16:23:05.072 [nioEventLoopGroup-2-1] DEBUG cn.wjb114514.heimaNetty.Channel.PureClient - [id: 0x1f442d95, L:/127.0.0.1:57981 - R:localhost/127.0.0.1:1145]
                log.debug("{}",channel);
            }
        });
    }
}


