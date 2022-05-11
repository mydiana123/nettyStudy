// 需求：如果服务器端写入的数据过大，导致客户端缓冲区满了，能不能在客户端缓冲区满的时候让服务器干别的，只有当客户端缓冲区空了，channel处于可写状态时，再去写
package cn.wjb114514.c3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

public class WriteServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(1145));
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                // 和客户端建立连接
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);

                    // 1.向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }

                    ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
                    int writeLen = sc.write(byteBuffer); // 返回值表示实际写入的数据
                    System.out.println(writeLen);

                    // 2.判断是否buffer还有剩余内容，有则表示 网络通信缓冲区无法一次性把buffer全读完
                    if (byteBuffer.hasRemaining()) {
                    /*
                    判断有没有剩余内容 要发送的数据太多了，把客户端的缓冲区都写满了...
                
                    当byteBuffer过于庞大，导致把传输缓冲区都写满了，byteBuffer还没写完，就会导致byteBuffer还有剩余
                    关注可写事件。[注意，要在原来关注的事件基础上，加上新的数字]
                    由于这些标记为就是 1 << k 所以可以通过相加来实现 标记的合取。
                     */
                        // 3.让客户端关注可写事件，即这时，selector会监听socketChannel上发生的可写事件。
                        /*
                        也就是说，既然你客户端现在满了，我服务器就先不轮询了，等你有空了我再来写。
                        我们的需求是： 现在因为某些问题，服务器无法一次性把数据写到客户端的channel里。因为用于数据传输的缓冲区满了，暂时写不了
                        如果按照原来的方式，服务器端会一直写缓冲区，如果写不进去就继续尝试写。
                        但我们知道，只有客户端把数据读完了，缓冲区空闲了，服务器才可以继续写入
                        所以需求就是 ===> 能不能在 缓冲区从满[不可写入] 到 空[可写入的过程中]，服务器不做无用功，去干点别的
                        而一旦客户端把缓冲区读空了，服务器再来写入。
                        处理方法就是：注册一个key，监听 客户端channel上的写事件。
                        如果客户端的缓冲区空了，说明此通道是可写的，只有此通道发生了可写的事件，服务器才继续向缓冲区里写入。否则就去干别的，
                        */
                        scKey.interestOps(scKey.interestOps() + SelectionKey.OP_WRITE);
                        // 要把未写完的数据挂到selectionKey上[附件]
                        scKey.attach(byteBuffer);
                    } else if (key.isWritable()) {
                        // 当key为isWriteable时，表示现在channel的缓冲区已经空闲了，客户端允许服务器继续写入
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        SocketChannel sc2 = (SocketChannel) key.channel();
                        int writeLen2 = sc.write(buffer);
                        System.out.println(writeLen2);
                        // 清理操作
                        if (!buffer.hasRemaining()) {
                          /*
                            // buffer为空
                            // 覆盖上一次关联的极大buffer。进行gc
                            // 这里的key就是 发生了可写事件的客户端的key，和上面的scKey一样，用哪个都可以
                           */
                            key.attach(null);
                            System.out.println("发生可写事件的key=" + key);
                            System.out.println("scKey =" + scKey);
                            // 删除可写事件
                            key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                        }
                    }
                }
            }
        }
    }
}