package cn.wjb114514.c3;

import java.nio.channels.Selector;

/**
 * 阻塞： 连接事件/IO事件 在没有连接建立/数据可读/可写 时，线程阻塞，此方法几乎不可以用于多线程
 * 非阻塞： 连接事件/IO事件 在没有连接建立/数据可读/可写 时，线程不阻塞。如果 没有连接/IO事件，则返回null/0
 * 多线程问题：
 * 1.32位jvm一个线程320K，64位一个线程1024k，连接数过多时，导致OOM，线程太多导致的上下文切换 也是一个使性能降低的问题
 * 2.虽然可以采取线程池，但是治标不治本，不适用于长连接。
 *
 * 非阻塞问题：线程的利用率比较低，因为线程无法确定 什么时候发生事件，只能不断轮询
 *
 * 多路复用： 单线程配合Selector完成对多个 Channel的可读写事件的监控，称之为多路复用、
 * 1.仅可用于网络IO，FileChannel用不了多路复用技术。
 *
 * 其实selector就是把 会导致阻塞的监听 工作包揽到自己身上了
 * 这里阻塞/非阻塞的实现，在JUC里应该有讲，等哪天去看看
 * 当然selector也可以做成非阻塞的，相当于如果监听不到事件就 返回0
 * 但是selector的目的就是为了监听事件的，所以最好还是做成阻塞的。
 *
 * selector何时不阻塞
 * 1.客户端发起连接请求，触发accept事件
 * 2.客户端发送数据过来，客户端正常/异常关闭时==> 触发read事件。
 * 另外如果发送数据大于目标主机设置的用来接受的buffer，会触发多次读取事件
 * channel可写时，触发write事件
 * 。如果在linux下发生了nio bug。也会导致selector非阻塞
 * 3.调用wakeup()方法
 * 4.调用close()方法
 * 5.selector所在线程interrupt()
 */
public class Conclusion {
    public static void main(String[] args) {

    }
}
