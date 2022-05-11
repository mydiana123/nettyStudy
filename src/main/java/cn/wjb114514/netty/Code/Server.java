package cn.wjb114514.netty.Code;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {
    public static void main(String[] args) {

        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        try {
            // 1.创建BossGroup和WorkerGroup，分别管理两个线程池

            // 两个都是无限循环 轮询

            // bossGroup 和workerGroup 含有子线程（NioEventLoop）个数 默认为实际cpu核数 * 2
            // 1.1 bossGroup只处理连接请求
            bossGroup = new NioEventLoopGroup(1);

            // 1.2与客户端的业务处理交给workerGroup
            workerGroup = new NioEventLoopGroup(8);

            // 2.创建服务器端启动助手，可以配置启动参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 使用链式编程设置启动参数
            serverBootstrap.group(bossGroup, workerGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 设置服务器端的通道使用NioServerSocketChannel此类型。
                    .option(ChannelOption.SO_BACKLOG, 128)  // 设置线程队列等待连接的个数 backlog--积压
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 使用匿名对象创建一个通道测试对象
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 此方法可以给pipeline设置处理器
                            socketChannel.pipeline() // 返回当前channel对应的pipeline channel<->pipeline
                                    .addLast(new ServerHandler()); // 向当前pipeline注册处理器，目前用null占位
                        }
                    }); // 给我们的workerGroup对应的管道设置处理器

            // 3.绑定一个端口，并且同步处理.返回一个ChannelFuture对象。 此时就启动了服务器，并绑定了端口
            ChannelFuture cf = serverBootstrap.bind(6668).sync();

            // 4.对关闭通道进行监听
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 如果发生了异常，则优雅关闭工作组
            if (bossGroup != null && workerGroup != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }

        }
    }
}
