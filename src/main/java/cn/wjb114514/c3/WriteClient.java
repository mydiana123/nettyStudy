package cn.wjb114514.c3;

//import io.netty.channel.socket.SocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",1145));
        int count = 0;
        // 接收数据
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            int readLen = sc.read(buffer);

            count += readLen;
            System.out.println(count);
            buffer.clear();
        }
    }
}
