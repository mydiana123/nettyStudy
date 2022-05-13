package cn.wjb114514.heimaNetty.Channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TestEmbeddedChannel {
    public static void main(String[] args) {
        // embedded channel 这个netty提供的工具类，可以直接绑定handler。不用再写客户端和服务器端
        ChannelInboundHandler h1 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("1");
                super.channelRead(ctx, msg);
            }
        };
        ChannelInboundHandler h2 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("2");
                super.channelRead(ctx, msg);
            }
        };
        ChannelOutboundHandlerAdapter h3 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("3");
                super.write(ctx, msg, promise);
            }
        };
        ChannelOutboundHandlerAdapter h4 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("4");
                super.write(ctx, msg, promise);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(h1, h2, h3, h4);
        // 其中，Client的一侧是head handler，而Server一侧是tail handler
        // channel.writeInbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello,world".getBytes(StandardCharsets.UTF_8))) ; // InBound是指 Client->Server 的数据流动 1 - 2
        channel.writeOutbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello,world".getBytes(StandardCharsets.UTF_8))) ; // OutBound是指 Server->Client 的数据流动 4 - 3
    }
}
