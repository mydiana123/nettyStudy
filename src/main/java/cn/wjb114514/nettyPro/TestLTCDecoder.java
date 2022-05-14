package cn.wjb114514.nettyPro;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

public class TestLTCDecoder {
    public static void main(String[] args) {
        EmbeddedChannel ch = new EmbeddedChannel(
                // 最长接受1024，从0开始就是长度字节，长度字节长度为4[INT],长度字节后隔一个字符才是内容。最后把前四个字节剥离
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 1, 4),
                new LoggingHandler(LogLevel.DEBUG)
        );
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer,"HELLO, WORLD");
        send(buffer, "HI!");

        ch.writeInbound(buffer);
    }
    public static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        buffer.writeInt(len);
        buffer.writeByte(1); // 版本号
        buffer.writeBytes(bytes);
    }

}
