package cn.wjb114514.nettyPro;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TestRedis {
    public static void main(String[] args) throws InterruptedException {
        // 模拟客户端 向Redis服务发起命令
        final byte[] LINE = new byte[]{13, 10};
        NioEventLoopGroup worker = new NioEventLoopGroup();
        new Bootstrap()
                .group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf buf = ctx.alloc().buffer();
                                buf.writeBytes("*3".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("$3".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("set".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("$4".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("name".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("$8".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                buf.writeBytes("zhangsan".getBytes(StandardCharsets.UTF_8));
                                buf.writeBytes(LINE);
                                ctx.writeAndFlush(buf);
                                super.channelActive(ctx);
                            }

                            // 我们发送的数据被Redis服务端成功接收，返回 +OK\r\n
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(((ByteBuf) msg).toString(StandardCharsets.UTF_8));
                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                }).connect("localhost", 6379)
                .sync().channel();
    }
}
