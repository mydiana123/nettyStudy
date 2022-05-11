package cn.wjb114514.netty.Code;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        // 1.客户端需要一个事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // 2.启动助手
            Bootstrap bootstrap = new Bootstrap();

            // 2.1给启动助手配置参数
            bootstrap.group(group)  // 设置线程组
                    .channel(NioSocketChannel.class) // 设置通道实现类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());    // 向管道加入handler处理器。处理器实现了一系列事件的回调函数。即发生某事件后就调用的函数。也可以认为是处理事件的函数
                        }
                    });         // 设置用于处理 客户端事件的回调函数

            // 3.启动客户端 连接服务器
            // 关于ChannelFuture涉及到netty的异步模型，等待分析
            ChannelFuture cf = bootstrap.connect("127.0.0.1", 6668).sync();

            // 4.关闭通道增加监听 ： 只有通道关闭事件发生时才关闭通道。
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
