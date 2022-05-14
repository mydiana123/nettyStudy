package cn.wjb114514.nettyPro;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Client {

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
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 当连接建立后 触发此方法

                                    for(int i = 0; i < 10; i++) {
                                        ByteBuf buf = ctx.alloc().buffer(16);
                                        buf.writeBytes(new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17});
                                        ctx.writeAndFlush(buf);
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

}
