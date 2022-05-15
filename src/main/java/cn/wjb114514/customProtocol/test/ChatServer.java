package cn.wjb114514.customProtocol.test;
import cn.wjb114514.customProtocol.protocol.MessageCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        // 此日志handler是线程安全的，可以共用
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodec MESSAGE_HANDLER = new MessageCodec();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    // 只能为每个channel都准备一个帧解码器，因为线程不安全
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,12,4,0,0));
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 问题 ==> 我们的自定义编解码器是不是线程安全的 ?  ==> 我们就怕发生了 黏包和半包 导致多个channel的数据被拼到一起
                    // 但是由于 帧解码器处理后的数据，到我们自定义编解码器后的数据已经是处理完黏包和半包的了，所以我们可以认为 自定义的编解码器是线程安全的
                    // 问题2 ==> 可不可以给我们已经确定的 线程安全的解码器 一个@Sharable注解呢?
                    // 不可以! 因为我们的编解码器 继承了一个ByteToMessageCodec。 如果初始化@Sharable 时，会导致父类构造器被加载
                    // 而父类编解码器 默认认为 ==> 所有的子类都有存在[缓存上次数据的可能性] 线程安全的风险，因此不允许任何子类被@Sharable标注
                    // ByteToMessageCodec == init() ==> this(true) == ensureNotSharable() ==> isSharable() ==> 为真报异常
                    // isSharable()的工作原理 标注了@Sharable注解的handler，会被加入一个缓存里，这个缓存都存放着被认为线程安全的handler
                    // 之后获取本类的class()对象，并从这个cache里取出本类class对象，如果找到了返回true。如果没找到，则查看class对象是不是被标注了@Sharable接口。如果标注了，就把此类和true关联。

                    // 所以 ==> 就算子类是线程安全的，也无法标注@Sharable
                    // 解决方法 ==> 换一个父亲 MessageToMessage<T> 这个父亲认为 此时经过编解码的信息已经不存在 数据缓存，也就是黏包半包问题了。所以这个类的子类不会进行@Sharable 的检查。
                    ch.pipeline().addLast(MESSAGE_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
