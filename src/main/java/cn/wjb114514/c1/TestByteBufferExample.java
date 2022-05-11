package cn.wjb114514.c1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

/**
 * 网络上有多条数据发送给服务端，这些数据使用\n (LF)分割。由于操作问题，接收时数据进行了重新组合
 * Hello,world!\n
 * I'm ZhangSan\n
 * How are you?\n
 *
 *  ==> Hello,world!\nI'm ZhangSan\nHo [黏包]
 *  w are you\n [半包]
 * 黏包发生的原因： 一次性把多个信息发送给服务器，服务器会把多个tcp包进行组合
 * 半包发生原因： 服务器的缓冲区满了，第二次接受的数据只能丢弃。也就是Ho后面的东西，之后缓冲区清空后，再把剩下内容接受
 *
 */
public class TestByteBufferExample {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world!\nI'm ZhangSan\nHo".getBytes(StandardCharsets.UTF_8));
        split(source);
        // 读到换行符就结束，因此此时Ho是未被读取的，下一次写应该从o后面写入，所以我们用compact()方法，把position移动到o的后面。~~~
        source.compact();
        source.put("w are you?\n".getBytes(StandardCharsets.UTF_8));
        split(source);
    }

    private static void split(ByteBuffer source) {

        source.flip(); // 读取source的数据
        for(int i = 0; i < source.limit(); i++) {

            if (source.get(i) == '\n') {
                // 获取到一条完整消息，把消息存入新的buffer
                // 计算本条信息的长度:换行符的下一位 - 处理之前的起始位置
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for(int j = 0; j < length; j++) {
                    // 由于从source搬数据时，要把source的position向后移动，所以我们要直接get() 不用get(i)。
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
    }
}
