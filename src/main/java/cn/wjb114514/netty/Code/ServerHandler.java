package cn.wjb114514.netty.Code;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

/**
 * 我们自定义一个handler，需要继承netty规定好的某个handlerAdapter。
 * 所谓handlerAdapter其实是老熟人了，在SpringMVC的源码里也 使用到了handlerAdapter。
 * handlerAdapter实现了 处理器 -适配器-> 处理方法 的适配器模式
 * 主要是用于针对不同种类的需要处理的业务，进行不同处理器方法的调用。
 * 在SpringMVC中，适配器的使用是为了 更好的决定什么样的Controller调用什么样的处理方法
 * 因为我们在使用Controller时，不知道其具体的运行类型，这就需要使用instanceof判断，并强转调用。
 * 而adapter就避免了大量的if-else，减少了硬编码性，增加了扩展性，实现了不同的controller调用不同的处理方法
 *
 * 其实可以看到，基于事件驱动的模型和 监听-回调函数很相似。
 * 只不过java本身不支持回调函数，所以可以由框架实现回调机制
 * 所谓回调是指发生某个函数A后，函数A调用某个函数B，我们把函数A认为是监听到的事件，函数B认为是对事件A的处理函数。
 * 这就满足回调的定义，即事件的发生导致了处理事件函数的发生
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     *
     * @param ctx 上下文对象，包含了管道pipeline[关联handler，数据流过pipeline时，会经过handler并被处理] 通道Channel 连接地址
     *            上下文对象在各个地方都很常见，这是一个线程相关的概念， 而线程之间的数据共享是不太容易做到的，想做到线程之间数据共享就要依赖于上下文对象
     * @param msg 客户端发来的数据，以对象形式。
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器读取线程：" + Thread.currentThread().getName());
        // 读取数据的事件，这里可以读取客户端发送的信息
        System.out.println("server ctx =" + ctx);
        System.out.println("看看pipeline和channel的关系");

        Channel channel = ctx.channel();
        // 底层本质是一个双向链表。出站入站问题
        ChannelPipeline pipeline = ctx.pipeline();


        // 处理msg对象，转换为可处理的对象
        // 此ByteBuf是netty提供的对象，不是NIO 的原生Buffer。此ByteBuf性能会更高
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送数据是" + ((ByteBuf) msg).toString(CharsetUtil.UTF_8));
    }

    // 回写数据的事件
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 此方法表示读取数据完毕后执行的回调函数
        // 一般而言，对发送的数据需要先进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端",CharsetUtil.UTF_8)); // 先把数据写到线程上下文的缓冲区，并刷新，刷新后客户端才可以得到数据
    }

    // 异常处理的回调函数
    // 一般发生异常就需要关闭通道


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
