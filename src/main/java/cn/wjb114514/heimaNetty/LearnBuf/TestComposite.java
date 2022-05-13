package cn.wjb114514.heimaNetty.LearnBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class TestComposite {
    public static void main(String[] args) {
        // 原始做法合并
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1,2,3,4,5});
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6,7,8,9,10});

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        // buffer.writeBytes(buf1).writeBytes(buf2); // 存在数据拷贝，效率不高

        // 解决
        CompositeByteBuf byteBufs = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 此方法在合并时。不会自动调整写指针，于是可读部分[read index -> write index]就为0 [read index = write index]
        // 维护性差，但是减少了数据的 拷贝 ==> 要注意子切片的 引用计数问题。
        // buf1.release(); 注意此操作~
        byteBufs.addComponents(true,buf1,buf2);
        log(byteBufs);

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
