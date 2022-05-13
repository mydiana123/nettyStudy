package cn.wjb114514.heimaNetty.LearnBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(20);
        buf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        log(buf);

        // 切片过程中没有发生数据复制。效率较高
        ByteBuf buf1 = buf.slice(0, 5);
        ByteBuf buf2 = buf.slice(5, 5);
        log(buf1);
        log(buf2);
        // buf1.writeByte('x');
        buf1.retain();
        buf.release();
        buf2.setByte(2,'a');
        // buf.slice(); // 无参的slice() 默认把 可读区间切片。 ==> 从读指针开始 到可读部分结束[即当前容量]
        int i = buf.readableBytes();
        System.out.println(i);
        // 独立操作切片 [每个切片有自己独立的读写指针，但内存地址仍然是原来的buf]
        buf1.setByte(2,'z');
        log(buf); // 原始的buf也变化了。

    }

    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }
}
