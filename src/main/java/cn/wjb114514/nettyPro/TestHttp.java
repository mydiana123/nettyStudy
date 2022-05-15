package cn.wjb114514.nettyPro;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * 使用netty提供的http编解码器，搭建http服务器端
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ChannelFuture cf = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<DefaultHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest msg) throws Exception {
                                    log.debug("请求的uri:{}",msg.uri());
                                    log.debug("请求的请求头:{}",msg.headers());

                                    log.info("返回响应");
                                    // 此对象必须指定version和状态码
                                    DefaultFullHttpResponse resp = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK);
                                    // 设置响应体
                                    byte[] data = "<h1>hello,world!</h1>".getBytes(StandardCharsets.UTF_8);
                                    // public static final AsciiString CONTENT_LENGTH = AsciiString.cached("content-length");
                                    resp.headers().setInt(CONTENT_LENGTH, data.length);
                                    resp.content().writeBytes(data);

                                    // 注意事项，
                                    // 1.http采取LTC编码，我们需要指定消息的长度，不然浏览器会一直等着更多的消息，表现为 一直转圈
                                    // 2.浏览器发送请求后，会自动请求项目路径下的favicon.ico
                                    ctx.writeAndFlush(resp);
                                }
                            });
//                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                                @Override
//                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                    log.debug("经过http解码器解码后的消息类型:{}",msg.getClass());
//                                    // class io.netty.handler.codec.http.DefaultHttpRequest [请求行+请求头]
//                                    // class io.netty.handler.codec.http.LastHttpContent$1 [请求体]
//                                    super.channelRead(ctx, msg);
//                                }
//                            });
                        }
                    }).bind(9999).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
