package cn.wjb114514.nettyPro;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EchoServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        new ServerBootstrap()
                .group(boss, worker)
                // .option(ChannelOption.SO_SNDBUF)
                // .option(ChannelOption.SO_RCVBUF, 10) // 设置serverSocketChannel的选项 ==> 设置接受缓冲区为10个字节
                // 此设置用于设置应用层缓冲区大小，childOption()相当于 局部参数，即针对特定的Channel设置的参数。
                // 而option()表示全局参数，即所有的channel都会设置这些参数。
                .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16,16,16))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 注意事项 ==> 此解码器 定长度的设置 取决于要发送数据的最大值 次解码器handler要放在log之前，因为数据要先经过解码后才能正确的被log打印
                        // ch.pipeline().addLast(new FixedLengthFrameDecoder(10));

                        ch.pipeline().addLast(new LineBasedFrameDecoder(256));
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    }
                }).bind(9999);
    }
}
