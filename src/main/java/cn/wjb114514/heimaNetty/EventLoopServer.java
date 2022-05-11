package cn.wjb114514.heimaNetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/*
其中h1和h2代表handler。我们可以为其分配一个专门的线程来处理
                        h1由主线程绑定的worker线程处理 h2由我们自定义的线程组里的线程处理
其实，Client的消息 经过head -> h1 -> h2 -> tail 这就是管道， 管道的本质就是一个双向链表
        Client --> head --> h1 --> h2 --> tail
                        <--    <--    <--
 */
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {

        EventLoopGroup group = new DefaultEventLoopGroup(2); // 创建一个只处理 普通任务的线程组
        new ServerBootstrap()
                // 1.EventLoop的细化==> Boss和Worker
                // 第一个参数就是BossEventLoop，只负责处理serverSocketChannel的accept()事件，第二个参数就是WorkerEventLoop，只负责socketChannel上的IO事件
                // 在多个NioEventLoop里，serverSocketChannel只会占用其中一个，所以无需指定监听accept()的NioEventLoop

                // 进一步细分 如果单个handler的处理事件长，由于一个worker监管多个channel。所以如果一个channel被handler阻塞，其他worker管理的channel就阻塞
                // 方法 ==> 创建一个独立的EventLoop对象。
                .group(new NioEventLoopGroup(/*此处可以不指定线程数*/),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // addLast可以接受多个参数 ==> 1.单独的eventLoop。 2.handler的名字 3.具体的handler对象
                        // 如果不单独指定 线程组，则使用主线程分配的worker线程执行handler操作。
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            // 关心读取事件

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 由于数据经过流水线时，没经过StringDecoder，目前msg是ByteBuf类型的
                                ByteBuf buffer = (ByteBuf) msg;
                                // 需要转换字符集，现在的客户端和服务器都在一起，所以无需转换
                                // 可以看到，启动器的流程都是不变的，需要我们做的就是。针对事件的监听，和对事件的不同处理
                                // 即处理器handler的逻辑是需要我们实现的。当然netty也提供了一系列好用的已经实现的handler
                                log.debug(buffer.toString(StandardCharsets.UTF_8));
                                // 让消息传递给下一个handler
                                ctx.fireChannelRead(msg);
                            }
                        });
                        // 为此pipeline 分配两个处理器
                        ch.pipeline().addLast(group,"handler2",new ChannelInboundHandlerAdapter(){
                            // 关心读取事件

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

                                ByteBuf buffer = (ByteBuf) msg;

                                log.debug(buffer.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .bind(1145);
    }
}

/*
结论：如果两个handler绑定同一个线程，直接调用
否则在handler1的线程thread1里，把要调用的handler2方法封装为任务对象，由下一个handler2的线程调用
    static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
    // NIO实现线程切换的原理 ==> 关键方法
    // 首先，数据要经过pipeline这个双向链表，首先head头指针进入执行链
        final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);

        // 此方法返回 pipeline双向链表的下一个节点[节点就是一个handler] 也就是返回当前handler1 的下一个handler2
        EventExecutor executor = next.executor();

        // inEventLoop():boolean ==> 当前handler1所处的线程，和上面获取的下一个handler2所处的线程是不是同一个线程。
        if (executor.inEventLoop()) {
        // 是同一个线程，直接在 当前handler1所在的线程里 调用下一个handler2的方法。相当于handler1和handler2都在同一个线程thread1里被调用。
            next.invokeChannelRead(m);
        } else {
        // 不是同一个线程，在handler1所在的线程thread1中，注意：这里只是把handler2的任务放到了任务队列里，之后handler2所在的thread2会取出并调用
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelRead(m);
                }
            });
        }
    }
 */