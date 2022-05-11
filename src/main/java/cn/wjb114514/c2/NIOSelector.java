package cn.wjb114514.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;
import static cn.wjb114514.c1.ByteBufferUtil.debugRead;

@Slf4j
public class NIOSelector {
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
        // 如果存在没处理完的数据，就把没读完的对数据进行压缩。比如hello,world!\n123456 那么这个123456就属于没处理完的数据，由于get(i)不涉及pos移动
        // 因此此时的compact就会把没处理完的数据搬到前面去...
        source.compact();
    }

    public static void main(String[] args) throws IOException {


        // 1.创建一个selector对象。可以管理多个channel
        Selector selector = Selector.open();

        // 2.建立channel和selector 的联系 [注册]

        ByteBuffer buffer = ByteBuffer.allocate(16);


        ServerSocketChannel ssc = ServerSocketChannel.open();
        // register的返回值SelectionKey代表事件发生后，可以获取事件本身，以及哪个channel发生了事件。
        // 故可以把key认为是获取事件的一个 “钩子”
        // 事件：accept[有连接请求时，服务器端触发此事件] connect[客户端建立连接后，触发此事件]
        // read[可读事件] write[可写事件]
        // 通常而言，sscKey 主要关注连接事件，所以需要关注accept事件
        ssc.configureBlocking(false);
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("注册在serverSocketChannel上的钩子key:{}", sscKey);
        sscKey.interestOps(SelectionKey.OP_ACCEPT); // 指明用于获取事件的key只会获取其关注的accept事件


        ssc.bind(new InetSocketAddress(1145));

        List<SocketChannel> socketChannels = new ArrayList<>();
        while (true) {

            // 3.selector负责 阻塞工作，监听事件发生，这样selector的监听工作和服务器的其他工作进行分离，服务器只负责处理事件，监听事件换个人做
            selector.select();

            // 4.处理事件，需要拿到事件集合
            // selectedKey()方法获取一个集合 Set<SelectionKey> 用于获取当前selector监听到的事件。
            // 这里不用增强for，因为遍历后要删除，事件处理完了就让事件滚蛋，要不然会导致一个事件被处理很多次。
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // key是selector用于管理 事件event-channel二元映射的工具，selector把监听到的事件和对应的channel进行绑定后 用SelectionKey对象统一管理。
            // 而用来管理某个通道的key对象就是我们注册到selector上的那个。因此注册时的key和监听到事件的key是同一个key
            // 都用来处理注册到selector上的通道上发生的事件。
            Iterator<SelectionKey> iter = selectionKeys.iterator();
            while (iter.hasNext()) {
                // 当前的key就是正在发生的事件
                SelectionKey key = iter.next();
                log.debug("当前正在发生的处理事件的钩子key:{}", key);

                // 通过key获取当前发生 被连接事件[accept]且绑定了此key 的channel，也就是我们的ssc
                // 先判断正在发生的事件类型，再进行处理
                if (key.isAcceptable()) {
                    ServerSocketChannel ssc2 = (ServerSocketChannel) key.channel();
                /*
                对服务器而言，事件主要是accept[处理建立连接请求]以及IO[处理读写请求]
                阻塞： 服务器负责监听和处理事件。 监听是阻塞的，没有当前正在监听的事件发生则不处理
                非阻塞： 服务器负责监听和处理事件，监听是非阻塞的，也就是说，监听和阻塞同时进行，可以支持多个用户连接
                selector：服务器只负责处理事件，selector负责监听事件，监听是阻塞的，也就是说，监听的工作交给selector进行
                服务器只负责从selector监听到的事件结果集里取出事件并处理。工作分离 各司其职
                 */
                    SocketChannel sc = ssc2.accept();// 可以看到监听工作交给别人做了，服务器端只负责处理事件。
                    sc.configureBlocking(false);
                    ByteBuffer bufferCilent = ByteBuffer.allocate(16);
                    // 在客户端channel注册到selector时：===> 给客户端channel分配一个buffer附件
                    SelectionKey scKey = sc.register(selector, 0, bufferCilent);
                    log.debug("处理客户端事件的key:{}", scKey);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 把数据读到缓冲区进行操作。可读是指客户端往自己的通道里写数据了，那么通道里存在一些数据，我们就说这个通道可读

                        // 获取当前通道的附件
                        ByteBuffer bufferCilent = (ByteBuffer) key.attachment();



                        int read = channel.read(bufferCilent); // 如果是正常断开，返回值-1
                        if (read == -1) {
                            key.cancel();
                        } else {
                            // 什么时候需要扩容，我们的split方法 如果没能执行成功，也就是数据超出了临时buffer
                            split(bufferCilent);
                            // 如果没能成功扩容，compact方法不会被触发，此时position=limit
                            if (bufferCilent.position() == bufferCilent.limit()) {
                                // 如果position没被消耗
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                // 把源数据拷贝到新的buffer
                                // 现在源buffer是在写，我们要变成读模式，并把数据读到新buffer
                                bufferCilent.flip();
                                newBuffer.put(bufferCilent);
                                // 把当前channel 的附件进行再关联

                                key.attach(newBuffer);
                            }
                            bufferCilent.flip();
                            // debugRead(bufferCilent);
                            System.out.println(Charset.defaultCharset().decode(bufferCilent));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();
                    }
                }
                iter.remove();

            }

        }
    }
}

/*
注意事项：
1.select()方法是阻塞的。
因为总得有一个人 啥也不干，光听着事件的发生，只不过现在交给Selector干了，因此select()方法是循环阻塞监听的。
不过当有未处理的事件时，select()会不断的把当前事件加入事件集合Set<SelectionKey> 中，
因此当有未处理事件时，select()是非阻塞的。...
如果真的有一个事件我们不想处理了，要调用cancel()方法取消此事件

2.selector可以管理多个channel [注意这些channel必须都设置为非阻塞]
因为channel的非阻塞就意味着会发生阻塞的 操作[等待连接事件/IO事件] 都会交给selector来进行监听~~
也就是监听的工作交给selector，处理结果的工作交给channel~~

selector的设计模式 是观察者模式，这种设计 监听事件的对象，大多数都用的观察者模式。
当我们 使用register方法时，就把一个观察者[key]注册到了 主体[selector]内部的集合里
SelectedKeys: 这个集合存放 [发生了事件的集合]，也就是selector监听到了有事件发生了，就把这些有事件发生 的key加入此集合
SelectionKeys: 这个集合存放 [可能会发生事件的集合] 也就是我们一开始注册的集合，selector一直监听这些集合

本例中： sscKey==>accept  ----> ssc
        scKey==>read     ----> sc

        如果监听到sscKey上发生了accept事件，就把事件扔到SelectedKeys，之后如果处理了，就把事件标记为处理完毕，但是[不把sscKey从selectedKey]里拿出来。
        这时候在scKey上发生了read事件，就把scKey扔到SelectedKeys，之后处理了，就把事件标记为处理完毕
        此时SelectedKeys集合里有 [sscKeys,scKeys]
        // 于是！==》如果此时又发生了一个读事件，我们就会遍历selectedKeys集合去处理里面的所有keys
        我们在遍历这个SelectedKeys时，就会遍历到sscKeys。但是当前待处理的事件是 read事件，也就是说没有客户端连接
        而accept()方法在非阻塞模式下，如果没连接，返回null、
        于是我们产生了空指针异常~
        解决方式就是：处理完一个事件后，要从selectedKeys集合里，把此事件对应的key移除

3.selector {
Set<SelectionKey> SelectionKeys // 所有key的集合
Set<SelectedKey> SelectedKeys // 发生事件的集合
}

4.为什么一个客户端断开连接后，服务器就挂了[远程主机强迫关闭了一个连接]
因为客户端要断开连接后，会发生read事件，因为要通知服务器自己溜了，要发一段信息
那服务器这边还隔着read呢，又开始遍历selectedKeys集合。人家channel都没了，read谁去，于是就会出异常。
解决方法：如果服务器发生了异常[一般就是客户端断开导致read异常]，就把客户端的key从集合里删除。以免再对一个不存在的channel进行read事件处理
如果光捕获不把事件从SelectionKeys删除，这样客户端关闭导致的读事件就会不断的反复，因为没有人能处理这个读事件。
所以就不断的打印异常
使用key的cancel方法，可以把key从SelectionKeys里删除。

5.问题又来了，客户端正常断开时，不会进入异常处理。这样客户端的key还在SelectionKeys集合里，但是和key关联的channel已经无了
这样就导致read事件又异常了
其实，如果正常连接断开，导致的读事件，我们用read方法读的时候，返回-1.
而没有数据返回0.所以无论是正常断开还是异常断开，我们都应该做出处理

6.问题又来了，我们把buffer做成16个字节，如果客户端的消息是>16字节的呢？
如果是英文，可以分批次读取。
但是是中文呢，由于中文在unicode码里 一个汉字是三个字节，
如果发了一个 你好，世界 ==> 15bytes
如果是 你好世界哈 呵和 ==> 这样，你好世界哈=15，呵的第一个字节就被拆分了，导致乱码
你好，世界� [15+1] 这个乱码是因为有一个汉字被拆分成 1+2个字节了，这样都无法显示
18:02:31.293 [main] DEBUG cn.wjb114514.c2.NIOSelector - 当前正在发生的处理事件的钩子key:sun.nio.ch.SelectionKeyImpl@3581c5f3
��我是哈哈� [2+12+2]
18:02:31.294 [main] DEBUG cn.wjb114514.c2.NIOSelector - 当前正在发生的处理事件的钩子key:sun.nio.ch.SelectionKeyImpl@3581c5f3
� [1]

7.处理消息边界
半包和黏包的解决，..
解决思路1 : 客户端和服务器端约定一个固定长度作为缓冲区[比如1024]容量
客户端发送的内容不够，就用空字符补齐。
缺点：缓存空间浪费。 网络空间受限。
解决思路2 : 客户端和服务器端用一个特殊的分隔符 [比如CRLF]
服务器端按照分隔符进行分割。需要一个临时的buffer来接受消息
就是我们之前处理的那种情况，使用临时buffer来对消息按照分隔符分割，之后我们能统计出每条信息的长度
再分配指定大小的buffer2接受数据。
问题：1.消息长度  大于临时的buffer 2.对临时buffer进行预处理[逐字符比较]，耗时耗力。
解决思路3 : 在客户端发送的数据里，有一个长度位 ==> 第一步读取此数据的长度有多长。第二步按照数据长度进行分配
http协议里就是按照此方式解决半包和黏包问题。
[TLV格式 ==> Type Length Value]
Content-Type
Content-Length
Content-Body

8.如果使用解决方案2里的方法，如何实现
问题：如果作为临时的接收数据的buffer的长度小于发送的信息怎么办
比如 临时buffer只有16字节，但我们的数据有0123456789abcdef3333\n
这样：数据会分两次读取， 0123456789abcdef 和3333\n
如果我们的buffer是局部变量，在循环读取的过程中， 3333\n就对应一个新的buffer。
因此我们的方法时： buffer做成非局部变量，保证多次循环里都是一个buffer。而且要考虑到buffer的扩容问题
解决方案1：如果把buffer放到for循环外边，会导致每个SocketChannel都可以操作。可不行。
我们需要让每一个buffer都只能被当前channel读取。
附件：selector的register方法，可以在注册channel时，注册到selectionKey上。
于是就形成了 (selector,channel,buffer) 三元组。~~！！
这样，每一个channel都有自己的附件buffer~~

9.其实key管理了 (selector,channel,buffer)这三元组。而且key可以监听事件的发生，
所以其实可以看到，key是一个强大的封装工具，封装了 三元组和事件机制
 */
