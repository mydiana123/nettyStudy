package cn.wjb114514.netty.Code;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    // 连接完毕后的执行的回调函数
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 当通道就绪时，就会触发该方法
        System.out.println("client " + ctx);
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,server：猫喵喵~", CharsetUtil.UTF_8));
    }

    // 有数据可读（服务器端发来数据）时的回调函数
    // 和js的事件 极为相似，js的button绑定一个事件和回调函数 onclick="callback()" 那么发生点击事件就会触发callback()函数
    // 这里也一样，相当于把ChannelRead事件和channelRead方法绑定，如果出现通道可读事件，就执行channelRead方法
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器回复 的信息是: " + msg);
        System.out.println("服务器的地址：" + ctx.channel().remoteAddress());
    }

    // 发生异常事件时的回调

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
