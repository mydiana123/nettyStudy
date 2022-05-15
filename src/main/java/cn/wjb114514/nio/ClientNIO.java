package cn.wjb114514.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ClientNIO {
    public static void main(String[] args) throws IOException {
        // 1.得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();
        // 2.设置非阻塞模式
        socketChannel.configureBlocking(false);
        // 3.提供服务器端的ip和端口进行链接
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        // 4.连接服务器(非阻塞)
        // 4.1连接失败的情况(可能服务器并没有上线，我们的客户端不会阻塞等待连接成功)
        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("如果不能连接上服务器，不会阻塞，可以在这里干别的事情");
            }
        }

        // 4.2连接成功后的操作
        String str = "Hello,TestRedis~";
        // byteBuffer提供了wrap方法，可以把一个字节数组包裹到buffer里，无需手动指定大小。生成的大小就是我们传入字节数组的大小
        ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8));
        // 把buffer数据写入
        socketChannel.write(byteBuffer);

        // 阻塞
        System.in.read();
    }
}
