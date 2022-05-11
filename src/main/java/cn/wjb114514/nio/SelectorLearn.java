package cn.wjb114514.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * 1.javaNIO是非阻塞的io，可以用一个线程，处理多个客户端连接请求，要想达到这一目的，就需要selector选择器
 * 2.Selector能够检测多个注册的通道上是否有事件发生，也就是多个Channel以事件的方式可以注册到同一个Selector
 * 也就是说Selector有某种数据结构 可以管理注册到他身上的多个Channel对象。Selector可能采取轮询等形式，监听注册到此Selector下Channel发生的事件
 * 3.只有一个通道[连接]真正有读写事件时，才进行读写，减少了不必要的阻塞
 * 4.不需要在多个线程之间切换，降低了线程切换的消耗，提高了效率
 *
 * 1.netty的io线程NioEventLoop[事件轮询线程]聚合了Selector(多路复用器) 可以同时并发处理成百上千个客户端连接
 * 2.如果线程在某个客户端socket进行读写操作时，但是没有数据可用，不会阻塞等待数据
 * 3.线程把非阻塞io节省的时间可以用于在其他通道上执行io操作
 * 4.无需因为阻塞而把某一线程挂起，充分提升io线程的运行效率。
 * 5.解决了传统同步阻塞io -连接 - 线程模型，架构性能，弹性伸缩能力，可靠性都得到极大提升
 *
 * Selector是一个抽象类，常用方法:
 * 继承关系: Selector -> AbstractSelector -> SelectorImpl
 * 1.open() 获得一个选择器
 * 2.select(long timeout) 监控所有注册到通道，当通道里有io操作时，就会获得selectKey，并加入到
 *  protected Set<SelectionKey> selectedKeys = new HashSet(); 集合
 *  服务器端拿到 selectedKeys时，遍历集合，反向获得Channel对象
 *
 * Returns the channel for which this key was created.  This method will
 * continue to return the channel even after the key is cancelled.
 *
 *  SelectionKey内部的方法channel();
 *  return  This key's channel
 *  public abstract SelectableChannel channel();
 *
 *

 Selector的select()方法的描述
 * Selects a set of keys whose corresponding channels are ready for I/O
 * operations.
 *
 * 这里的blocking指的是:Selector会一直监听管道的事件，就好像serverSocket的accept()方法会一直监听等待客户端链接
 * 在监听到事件之前，selector的select方法是阻塞的。
 * 但是我们可以设置超时时间，以避免阻塞时间过长。
 * selectNow() 这个方法提供非阻塞的，如果没能监听到事件，不会等待，立即返回
 * wakeup()方法 可以唤醒selector 会让监听中的selector立即返回。不再继续监听事件的发生~
 *
 * <p> This method performs a blocking <a href="#selop">selection
 * operation</a>.  It returns only after at least one channel is selected,
 * this selector's {@link #*wakeup wakeup} method is invoked, the current
 * thread is interrupted, or the given timeout period expires, whichever
 * comes first.
 *
 * <p> This method does not offer real-time guarantees: It schedules the
 * timeout as if by invoking the {@link Object#wait(long)} method. </p>
 *
 * @param  *timeout  If positive, block for up to <tt>timeout</tt>
 *                  milliseconds, more or less, while waiting for a
 *                  channel to become ready; if zero, block indefinitely;
 *                  must not be negative
 *
 * @return  The number of keys, possibly zero,
 *          whose ready-operation sets were updated
 *
 * @throws java.io.IOException
 *          If an I/O error occurs
 *
 * @throws java.nio.channels.ClosedSelectorException
 *          If this selector is closed
 *
 * @throws  IllegalArgumentException
 *          If the value of the timeout argument is negative

public abstract int select(long timeout)
        throws IOException;
 */
public class SelectorLearn {
    public static void main(String[] args) throws IOException {
        // 1.创建serverSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.得到一个Selector对象
        Selector selector = Selector.open();
        // 3.通过channel获取socket，并绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        // 4.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 5.把serverSocketChannel注册到Selector(selector会监听 客户端向服务器建立连接请求的事件)
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 6.循环监听各通道的事件
        while (true) {
            // 这里可以采取selectNow()
            if (selector.select(1000) == 0) {
                // 等待1s仍没事件发生
                System.out.println("服务器阻塞1s，仍没监听到事件");
                continue;
            }
            // 如果监听到了事件[select()方法返回值大于0]，就获取到了相关的selectionKeys集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 通过channel()方法反向获取通道。
            Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
            while (selectionKeyIterator.hasNext()) {
                // 获取到各个selectionKey，并判断发生的事件类型，做出不同处理
                SelectionKey key = selectionKeyIterator.next();
                if (key.isAcceptable()) {

                    // 有新的客户端建立了 向服务器端的连接请求
                    // 那么给该客户端生成一个socketChannel。
                    // 传统的accept：一直阻塞等待连接
                    // NIO：只有当连接事件发生时，才会进行监听，这样相当于有连接请求的时候监听，并立即生成socketChannel
                    // 没有连接请求时干别的，这就是非阻塞的IO。虽然accept方法是阻塞的，阻塞的目的就是等待一个客户端的连接
                    // 现在我们有一个客户端的连接已经板上钉钉了，accept会立即的监听到这个连接，之后退出阻塞~
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 因为服务器是非阻塞模式，所以要把客户端的socketChannel也指定为非阻塞的。否则会java.nio.channels.IllegalBlockingModeException
                    socketChannel.configureBlocking(false);
                    // 将当前的socketChannel也注册到selector上 [!同时给该socketChannel关联一个buffer]
                    // 那么接下来，对此管道的读事件，都会现在Buffer里缓冲~
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                    System.out.println("客户端连接成功，生成一个socketChannel，地址为:" + socketChannel.hashCode());

                }
                if (key.isReadable()) {
                    // 发生读取事件 [服务器读取客户端数据]
                    // 1.通过key反向获取channel对象
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 2.获取到该channel关联的buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    // 3.把当前通道数据读取到buffer里
                    channel.read(buffer);
                    System.out.println("收到客户端发来的数据...:" + new String(buffer.array()));
                    // 从迭代器里删除该key:因为我们可能发生在多线程的情况下，处理完一个事件，就把其从任务队列[selectedKeys]里删除。避免重复处理
                }
                // 这句话要在所有if语句外面写，即发生任何事件 处理完毕后，都要移除
                // 不然就会在进入处理完的事件 在处理一遍，发生空指针异常
                selectionKeyIterator.remove();
            }
        }


    }
}

/*
1.Selector注册很多Channel
注册方法:服务器端的serverSocketChannel监听，当客户端连接时，生成socketChannel对象。
此时，服务器端把生成的socketChannel对象放入Selector，完成注册
    public final SelectionKey register(Selector sel, int ops)
        throws ClosedChannelException
    {
        return register(sel, ops, null);
    }
    注册后，返回一个SelectionKey，一个Selector上可以注册多个socketChannel
    返回的SelectionKey，会和该Selector关联
    protected Set<SelectionKey> selectedKeys = new HashSet();
2.Selector进行监听 select() 返回其管理的通道，哪些发生了事件。返回有事件发生的通道个数
ops: SelectionKey.READ WRITE CONNECTION ACCEPT 分别监听读事件，写事件，连接建立事件，发生连接事件
CONNECTION表示一个连接完成创建，ACCEPT表示有一个客户端连接到了服务器
3.进一步得到各个有事件发生的~~SelectionKey，再通过SelectionKey的channel()方法，反向获取注册的channel.
4.通过得到的Channel[socketChannel]完成业务处理
 */
