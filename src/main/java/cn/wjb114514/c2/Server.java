package cn.wjb114514.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.wjb114514.c1.ByteBufferUtil.debugRead;

@Slf4j
// 使用nio理解非阻塞
/**
 * 本案例中，有两个阻塞方法
 *
 * 服务器线程 ==> 阻塞在accept(),线程挂起，等待连接 =客户端连接=> 线程被激活,[从上一次阻塞的地方继续运行，涉及到上下文切换]，阻塞在read()，线程挂起，等待数据发送
 * =客户端发数据=> 线程被激活，由于while循环，继续执行到accept()方法被挂起，等待下一次连接 ==> ...
 *
 * 单线程处理多连接
 * 服务器线程 ==> 阻塞accept() =c1连接=> 阻塞在read() =c2连接=> 处理不了！ 因为目前阻塞在了read()方法上。
 * 只要c1不发来数据，就无法进入accept() 这样c2就只能干等着，
 * 所谓阻塞模式，是指两个客户端由于阻塞方法而互相影响彼此，无法很好的执行代码
 *
 * 服务器非阻塞：ssc.configureBlocking(false);
 * accept()方法 可以不阻塞，也就是我不傻等着你来连接，我可以在等你连接的同时去干别的，如果你不连接，我就认为sc=null、
 * 客户端非阻塞：sc.configureBlocking(false)
 * read()方法可以不阻塞。
 * 不阻塞的最大特色就是：可以允许不建立连接[不读到数据]
 * 也就是说阻塞模式必须等到你给我建立连接，你给我发送数据
 * 而费阻塞模式允许 "空"的情况发生，也就是 ： 就算你不给我连接，你不给我发数据，我也正常运行，只不过我把连接认为是null，数据认为读到了0、该干啥干啥，
 * 你的行为 [!不对我造成影响]
 *             if(sc != null) {
 *                 socketChannels.add(sc);
 *                 // 如果获取到了非null，也就是真正的客户端连接，我们可以通过获取到客户端的socketChannel，设置其为非阻塞
 *                 sc.configureBlocking(false); // 客户端的非阻塞对应read()方法的非阻塞~
 *             }
 *
 * 在非阻塞模式：
 *
 * 客户端 ===> {不断检查有没有连接请求，不断检查各个连接中有没有数据输入} =目前没有客户端连接=> {客户端继续轮询} =c1连接=> 把c1纳入管理
 * ===>{继续轮询} =c2连接=> 把c2加入管理 =c1发数据=> 处理数据 ===> {继续轮询}
 *
 * 阻塞模式和非阻塞模式就是两个极端
 * 阻塞模式就是 没有连接请求/读写请求就干等着啥也不干。 非阻塞模式就是 不管有没有请求，我都一直轮询，就算目前一个客户端的IO/连接请求都没有，我服务器仍然一直循环
 * 都不休息一秒钟的，劳碌命。
 *
 * 有没有一种更好的办法，可以让服务器端不用一致轮询，而是把轮询的操作交给别人，只有轮询者发现了事件的发生，才交给服务器处理呢
 * 可以看到非阻塞模式的服务器，一人分饰两角。不仅轮巡监听还负责处理，天天007，CPU资源浪费严重~
 */
public class Server {
    public static void main(String[] args) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2.绑定端口
        ssc.bind(new InetSocketAddress(1145));

        // 管理所有客户端的连接socketChannel对象
        List<SocketChannel> socketChannels = new ArrayList<>();
        while (true) {


            // 由于非阻塞模式在监听的过程中还可以干别的，所以监听语句就先不打印了
            // log.debug("connecting..."); // [在阻塞模式下accept阻塞在这里，没有客户端连接就等着，线程停止运行。]
            // 3.不断监听连接请求，accept方法就实现了tcp的三次握手四次挥手,并可以在服务器端获取客户端的socketChannel
            SocketChannel sc = ssc.accept();
            // 当客户端连接后，12:17:26.809 [main] DEBUG cn.wjb114514.c2.Server - after connected...java.nio.channels.SocketChannel[connected local=/127.0.0.1:1145 remote=/127.0.0.1:50726]

            if(sc != null) {
                // 真正建立连接才打印。
                log.debug("after connected...{}",sc);
                socketChannels.add(sc);
                // 如果获取到了非null，也就是真正的客户端连接，我们可以通过获取到客户端的socketChannel，设置其为非阻塞
                sc.configureBlocking(false); // 客户端的非阻塞对应read()方法的非阻塞~
            }
            for(SocketChannel socketChannel : socketChannels ) {
                // 不断接受各个客户端发来的数据

                // read方法也是阻塞的，不占用CPU事件，因为客户端没有向服务器发数据，没数据就等着，啥也干不了
                int readLen = socketChannel.read(buffer); // 非阻塞：如果客户端不发数据，返回值为0。这样就不傻等着了，可以干别的
                if (readLen > 0) {
                    // 真正读取到数据才打印
                    log.debug("before read....");
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
                    log.debug("after read...");
                }
            }
        }
    }
}

/*
SelectionKey对象是对事件的一个封装，selector负责监听事件，并把监听到的事件Event封装为SelectionKey对象
因为服务器的业务逻辑针对单独的event对象不太好处理，所以NIO设计者把事件对象封装为SelectionKey。
封装的目的就是为了提供 更全面的操作功能，比如SelectionKey在封装事件时，把发生事件的channel也封装了
这样服务器端代码就可以通过channel进行定向操作。
同时selectionKey也提供了操作事件，以及事件相关对象的一系列方法，这就是封装的好处。
 */