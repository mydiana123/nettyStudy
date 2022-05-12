package cn.wjb114514.heimaNetty.handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 1.获取channel绑定的流水线pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2.添加处理器，netty在创建pipeline时，会自动添加两个handler。即head和tail。我们添加的handler，按照先后顺序添加到head后，tail前
                        // head <-> handler1 <-> handler2 <-> handler3 <-> tail (pipeline是一个双向链表)

                        // [简单演示下多个handler共同工作 ==> ]
                        pipeline.addLast("handler1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("入站处理器handler1。对类型为ByteBuf的数据进行初步处理");
                                ByteBuf buf = (ByteBuf) msg;
                                String s = buf.toString(StandardCharsets.UTF_8);
                                // 怎么把处理完的数据s，传递给下一个入站处理器?

                                /*
                                    // 如果不调用fireChannelRead()方法，就会导致 执行链断了。 也就是，handler1的处理后的结果，就不会传递给handler2
                                    [职责链模式?? 使得功能的加工具有层次性。]
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                         ctx.fireChannelRead(msg);
                                    }
                                 */
                                // 把我们初步处理的s，发送给下一个handler2
                                super.channelRead(ctx, s);
                            }
                        });
                        pipeline.addLast("handler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            // 此时，这个handler2获取的msg，就是上一个handler1处理的s
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("数据进入入站处理器2，此方法对读取到的数据进行第二次加工，封装为一个学生对象");
                                Student student = new Student((String) msg);
                                // 把student对象 传给下一个channel
                                super.channelRead(ctx, student);
                            }
                        });
                        // 此时msg就是handler2处理的student对象
                        pipeline.addLast("handler3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("数据进入入站处理器3，此方法对读取到的数据进行操作");
                                log.debug("在handler3向客户端channel内写入一些数据,看看是不是会被出站处理器处理到~~");
                                ch.writeAndFlush(ctx.alloc().buffer().writeBytes("你好呀客户端，我是server".getBytes(StandardCharsets.UTF_8)));

                                System.out.println("============");
                                log.debug("看看handler3获得的数据是:{}" , (Student)msg);
                                // super.channelRead(ctx, msg); ==> 这步可以省略，因为没有下一个入站处理器了。
                            }
                        });

                        // 3.在管道里注册出站处理器
                        // 出站处理器 write()方法是 向channel写数据时的 回调函数
                        // head <-> h1 <-> h2 <-> h3 <-> h4 <-> h5 <-> h6 <-> tail
                        // 出站处理器 从tail -> head 的方向遍历 [只有数据真正的从channel里写入了，才会触发write()回调函数] ==> 也就是当前处理器所在的channel，向其他channel写数据
                        // 入站处理器 从head -> tail 的方向遍历 [只有其他的channel，向入站处理器所在的channel内写了数据，才会触发readChannel()回调函数...]
                        pipeline.addLast("handler4",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("数据进入 出站处理器4，此方法对读取到的数据进行操作");
                                super.write(ctx,msg,promise);
                            }
                        });

                        pipeline.addLast("handler5",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx ,Object msg, ChannelPromise promise) throws Exception {
                                log.debug("数据进入 出站处理器5，此方法对读取到的数据进行操作");
                                super.write(ctx,msg,promise);
                            }
                        });
                        pipeline.addLast("handler6",new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("数据进入 出站处理器6，此方法对读取到的数据进行操作");
                                // 如果不加这句话，会导致无法继续传递。 大概返回值 应该是一个 类似继续进行流水线 的信号。
                                super.write(ctx,msg,promise);
                            }
                        });
                    }
                }).bind(1145);

    }
    static class Student {
        private String s;

        public Student(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "s='" + s + '\'' +
                    '}';
        }
    }
}
