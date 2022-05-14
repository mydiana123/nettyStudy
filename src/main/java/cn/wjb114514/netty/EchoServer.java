package cn.wjb114514.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EchoServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 我们的回声服务器，既要支持入站[接受客户端的数据]操作，也要支持出站[把收到的数据原封不动的发回去]操作
                        ChannelPipeline pipeline = ch.pipeline();
                        // 给流水线注册工序
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 接收客户端的数据的方法。
                                super.channelRead(ctx, msg);
                                ByteBuf buf = ctx.alloc().buffer();
                                // 向客户端发送原封不动的数据 [入栈操作会把buffer传递给tail handler，因此无需手动释放]
                                log.debug("向客户端发送数据{}",msg);
                                ch.writeAndFlush(msg);
                            }
                        });
                        pipeline.addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("加工向客户端发送的数据{}",msg);
                                // 这个方法用于向客户端写入数据的出站操作
                                // 入站处理器 写入数据，触发此方法，我们要把服务器写出的数据转换为字符串
                                // 此时的msg就是 上面的buf对象
                                ByteBuf buf = (ByteBuf) msg;
                                String echoData = buf.toString(StandardCharsets.UTF_8);
                                // 注意，我们接下来把一个字符串传给head handler。这时候buf对象head handler就不会帮我们释放了
                                // echoData的流动 出战处理器 ==> head处理器 ==> 客户端channel
                                // 因此手动释放
                                buf.release();
                                super.write(ctx, echoData, promise);
                            }
                        });
                    }
                }).bind(9999);
    }
}
