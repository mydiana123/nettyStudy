package cn.wjb114514.c1;

import java.nio.ByteBuffer;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

public class ByteBufferTest2 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61);
        debugAll(buffer);
        buffer.put(new byte[]{0x62,0x63,0x64});
        debugAll(buffer);

        buffer.flip();
        System.out.println(buffer.get());
        debugAll(buffer);
        // 目前我们没读完，但是需要强制转换为写模式，会把没读完的数据前移。但是不会把移动后的数据清零，因为我们写入后会从未读完数据最后一个的后面开始写，就会把他覆盖
        buffer.compact();
        debugAll(buffer);
        buffer.put((byte)0x99);
        debugAll(buffer);

    }
}
