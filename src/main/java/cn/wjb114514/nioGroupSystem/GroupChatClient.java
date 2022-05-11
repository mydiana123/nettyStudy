package cn.wjb114514.nioGroupSystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class GroupChatClient {

    public static void main(String[] args) throws IOException {
        // 启动客户端
        GroupChatClient chatClient = new GroupChatClient();
        // 启动一个线程，实现数据的读取工作(读写分离，不共同阻塞)
        new Thread(()->{
            while (true) {
                // 不停读取数据
                chatClient.readInfo();
                try {
                    Thread.currentThread().sleep(3000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }
    }
    // 一个类的基本步骤:定义一些属性，完成一些初始化工作，定义一些方法完成业务逻辑
    private final String HOST = "127.0.0.1";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    public GroupChatClient() throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST,PORT));
        socketChannel.configureBlocking(false);
        // 只会监听其他通道的读事件，也就是说如果其他主机向他们的通道内写入数据，他们的通道就变为可读通道，这时这些通道就会被我们监听并读取
        // 其实服务器端线程通过selector监听所有客户端，客户端也通过selector监听服务器端 的事件。
        // 由于客户端和客户端之间没有通道和选择器，所以客户端和客户端的通讯实际上是经过了服务器端作为中介进行的
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 格式为 /ip:port
        username = socketChannel.getLocalAddress().toString();
        System.out.println(username + "is ok ...");
        System.out.println(socketChannel);
        System.out.println(socketChannel.hashCode());
    }

    // 向服务器端发送信息
    public void sendInfo(String info) {
        info = username + "说" + info;
        try {
            // 我们向通道里写入数据，写入的操作不会被选择器监听，因为我们设置选择器只用来监听读事件
            // 写入后，我们当前的客户端通道变成可读的，因为现在的通道是有数据的，别人可以从我们的通道里读取数据
            // 于是服务器端的selector就会监听到可读的通道，并从我们客户端这里读取数据
            socketChannel.write(ByteBuffer.wrap(info.getBytes(StandardCharsets.UTF_8)));
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 读取从服务器端回复的数据
    /*
    方法逻辑：通过和客户端通道关联的选择器对象，监听哪个通道是可读的，并从可读的通道读取数据。
     */
    public void readInfo() {
        try {
            // 如果有别的工作要做，可以设置超时时间，不然会阻塞在监听处
            int readChannels = selector.select();
            if (readChannels > 0) {
                // 有发生事件 的通道，我们只对发生了读事件 的通道进行处理
                // channel.read()的对象是channel，因此这里的读事件是指别人要读取通道里的数据，也就是我们要读取别人通道里的数据
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        // 得到相关的通道
                        // 从可以读的通道里读取数据
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        sc.read(buffer);
                        String msg = new String(buffer.array());
                        System.out.println(msg.trim());
                    }
                    keyIterator.remove();
                }
            }else  {
                System.out.println("目前没有可以用来读取的通道");
            }
        }catch (IOException e) {

        }
    }
}
