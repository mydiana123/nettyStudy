package cn.wjb114514.nio;

import java.nio.IntBuffer;

/**
 * 举例说明buffer的使用
 * 使用ctrl+h(hierarchy)查看类图，可以看到Buffer有很多实现子类
 * 比较：
 * 1.BIO以流的方式处理数据，而NIO以块的方式处理数据
 * 2.BIO是阻塞的，NIO是非阻塞的
 * 3.BIO基于字节流和字符流进行操作，NIO基于Channel和Buffer进行操作，数据总是从通道读取到缓冲区里，或者从缓冲区写入到通道(Channel <=> Buffer)
 * Selector用于监听多个通道的事件(连接请求，数据到达等~)，因此一个线程可以管理多个通道(客户端)
 *
 * 三者关系
 * 1.每个Channel都对应一个Buffer
 * 2.一个Thread管理一个Selector，一个Selector管理多个Channel(可以认为Channel就是C-S的连接)
 * 3.把Selector绑定Channel的行为，叫做Channel注册到某Selector
 * 4.程序决定切换到哪个Channel，是由事件驱动的(Event Driven)
 * 5.Selector会根据不同的事件，在各个不同的Channel上切换
 * 6.Buffer本身就是一个内存块，底层是一个数组
 * 7.数据的读取写入 是通过buffer的。这个和BIO有本质的不同。BIO中要么是输入流要么是输出流，不能双向流通，而NIO的buffer是可读可写的，但是需要使用flip()方法切换读写状态
 * 8.Channel也是双向的，可以反映底层操作系统的情况。比如Linux底层的操作系统通道就是双向的
 *
 */
public class BasicBuffer {
    public static void main(String[] args) {
        // 1.创建一个buffer,指定初始容量，本例可以存放5个数据
        IntBuffer intBuffer = IntBuffer.allocate(5);

        // 2.向buffer里存入数据
        intBuffer.put(10);
        intBuffer.put(11);
        intBuffer.put(12);
        intBuffer.put(13);
        intBuffer.put(14);

        // 3.从buffer里读取数据
        intBuffer.flip(); // 读写切换

        // 类似迭代器的遍历
        while(intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }
    }
}
