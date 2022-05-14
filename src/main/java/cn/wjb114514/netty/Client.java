package cn.wjb114514.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Client {

    @SneakyThrows
    public static void main(String[] args) {

        Channel ch = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println("===收到服务器发来的消息===");
                                System.out.println(msg);
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .connect("localhost", 9999)
                .sync()
                .channel();

        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("亲~请问您想说什么呀");
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    ch.close();
                    log.debug("处理关闭后的善后工作");
                    break;
                }
                ch.writeAndFlush(line);
            }
        },"input").start();
    }

}
