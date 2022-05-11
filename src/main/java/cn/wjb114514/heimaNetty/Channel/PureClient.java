package cn.wjb114514.heimaNetty.Channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

public class PureClient {
    public static void main(String[] args) throws InterruptedException {

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
         */
        Channel channel = channelFuture/*.sync()*/.channel();
        channel.writeAndFlush("hello,world");
    }
}


