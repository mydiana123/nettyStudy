package cn.wjb114514.nettyPro;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;

import java.util.Random;

public class DecoderClient {

    public static byte[] fill10Bytes(char c, int len) {
        byte[] b = new byte[10];
        for (int i = 0; i < len; i++) {
            b[i] = (byte) c;
        }
        for(int j = len; j < 10; j++) {
            b[j] = '_';
        }
        return b;
    }

    @SneakyThrows
    public static void main(String[] args) {
        NioEventLoopGroup group = null;
        try {
            group = new NioEventLoopGroup();
            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ByteBuf buf = ctx.alloc().buffer();
                                    char c = 'a';
                                    for (int i = 0; i < 10; i++) {
                                        byte[] bytes = fill10Bytes(c, new Random().nextInt(10));
                                        c++;
                                        buf.writeBytes(bytes);
                                    }
                                    ctx.channel().writeAndFlush(buf);
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
}
