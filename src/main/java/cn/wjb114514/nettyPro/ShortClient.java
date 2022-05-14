package cn.wjb114514.nettyPro;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ShortClient {

    // 使用短连接发送数据
    public static void send() {
        NioEventLoopGroup group = null;
        try {
            group = new NioEventLoopGroup();
            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 当连接建立后 触发此方法

                                    for (int i = 0; i < 10; i++) {
                                        ByteBuf buf = ctx.alloc().buffer(16);
                                        buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
                                        ctx.writeAndFlush(buf);
                                        // 短连接 ==> 写完一次就把连接关闭一次
                                        ctx.channel().close();
                                    }
                                    super.channelActive(ctx);
                                }
                            });
                        }
                    })
                    .connect("localhost", 9999);
            // 异步获取channel对象，同步等待关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            assert group != null;
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        for(int i = 0; i < 10; i++) {
            send();
        }
        System.out.println("发送完毕");
    }
}
