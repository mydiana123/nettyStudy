package cn.wjb114514.nioGroupSystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GroupChatServer {

    // 定义相关属性
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;

    // 初始化工作
    public GroupChatServer() {
        try {
            // 1.得到选择器
            selector = Selector.open();
            // 2.初始化serverSocketChannel
            listenChannel = ServerSocketChannel.open();
            // 3.绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            // 4.设置非阻塞模式
            listenChannel.configureBlocking(false);
            // 5.注册Selector - Channel二元组 监听客户端 连接事件
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Selector监听到事件的处理方法
    public void listen() {
        try {
            while (true) {
                // 监听事件的发生
                int count = selector.select(2000);
                if (count > 0) {
                    // 有待处理的时间
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        // 针对当前selectionKey管理的Channel发生的事件，做不同的处理
                        if (key.isAcceptable()) {
                            // 获取客户端的socketChannel对象
                            SocketChannel sc = listenChannel.accept();
                            // 注册三元组,监听读事件
                            // 注意：客户端和服务器的阻塞状态应保持同步，所以给客户端生成的socketChannel也要设置为非阻塞
                            System.out.println(sc);
                            System.out.println(sc.hashCode());
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ);
                            // 生成客户端上线的提示信息
                            System.out.println(sc.getRemoteAddress() + "上线了~");
                        }
                        if (key.isReadable()) {
                            // 通道是可读的状态(当前注册的某个通道发生读事件，可能是服务器端读客户端的，也可能是客户端读服务器端的)
                            readData(key);
                        }
                        // 删除已经处理完的事件，防止重复处理
                        iterator.remove();
                    }
                } else {
                    System.out.println("服务器端 等待ing...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取客户端消息
    private void readData(SelectionKey key) {
        SocketChannel channel = null;
        try {
            // 通过key取到关联的channel
            channel = (SocketChannel) key.channel();
            // 创建一个缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int count = channel.read(buffer);
            if (count > 0) {
                String msg = new String(buffer.array());
                System.out.println("from 客户端: " + msg);
                // 向其他客户端转发消息
                sendInfoToOrderClients(msg,channel);
            }
        } catch (IOException e) {
            // 如果没能成功读取客户端数据，很有可能客户端已经掉线，因为是非阻塞的，客户端的数据不一定立即被处理，可能客户端发完之后就下线了
            try {
                System.out.println(channel.getRemoteAddress() + "离线...");
                // 取消注册，关闭通道
                key.cancel();
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // 给其他通道(去掉服务器本身)发消息
    private void sendInfoToOrderClients(String msg, SocketChannel self) throws IOException {
        System.out.println("服务器开始转发消息");
        // 遍历所有注册到selector上的socketChannel并排除自己
        for (SelectionKey key : selector.keys()) {
            Channel channel = key.channel();
            // 我们的目标通道是客户端通道socketChannel
            if (channel != self && channel instanceof SocketChannel) {
                SocketChannel dst = (SocketChannel) channel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                dst.write(buffer);
            }
        }
    }

    public static void main(String[] args) {
        // 创建服务器对象，并进行监听
        new GroupChatServer().listen();
    }
}
