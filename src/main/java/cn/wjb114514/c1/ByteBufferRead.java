package cn.wjb114514.c1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

public class ByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a','b','c','d'});
        buffer.flip();

        // 把数据从buffer读取到 byte[4] 数组里
        buffer.get(new byte[4]);
        debugAll(buffer);
        // rewind:从头开读 position=0 不切换模式
        buffer.rewind();
        debugAll(buffer);

        // mark & reset mark：记录position的位置 reset：把position置为mark标记的位置。
        // 也就是说mark的作用就是标注读的起点，可以实现特定位置的反复读取

        buffer.get();
        buffer.get();
        // 目前position = 2
        buffer.mark(); // mark记录当前position=2的位置
        System.out.println(buffer.get());
        buffer.reset(); // 把position恢复到当前的mark位置
        System.out.println(buffer.get());

        // get(i)方法：不修改position，读取指定索引位置的元素~

        // byteBuffer和String的互相转换。

        System.out.println("----------------------");
        // 字符串转buffer
        ByteBuffer buffer1 = ByteBuffer.allocate(30);
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        buffer1.put(bytes);
        debugAll(buffer1);
//        "hello".getBytes(StandardCharsets.UTF_8)

        // 2.Charset
        ByteBuffer encode = StandardCharsets.UTF_8.encode("你好，世界！");
        buffer1.put(encode);
        debugAll(buffer1);

        // 3.wrap

        ByteBuffer wrap = ByteBuffer.wrap("hello,world".getBytes(StandardCharsets.UTF_8));
        debugAll(wrap);

        buffer1.flip(); // 切换为读模式
        System.out.println(StandardCharsets.UTF_8.decode(buffer1).toString());
    }
}
