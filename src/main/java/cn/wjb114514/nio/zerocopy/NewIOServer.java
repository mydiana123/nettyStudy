package cn.wjb114514.nio.zerocopy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NewIOServer {
    public static void main(String[] args) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(6667);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(inetSocketAddress);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        while (true) {
            // 拿到连接到服务器的客户端的socketChannel对象
            SocketChannel socketChannel = serverSocketChannel.accept();

            int readCount = 0;
            while (-1 != readCount) {
                readCount = socketChannel.read(byteBuffer);
                byteBuffer.rewind();
                // 读写转换用flip+clear。连续读/写用rewind。
                // 倒带：position=0,mark=-1 相当于把bytebuffer倒带，以便下次继续读取
                FileChannel fileChannel = new FileOutputStream("./a.zip").getChannel();
                fileChannel.write(byteBuffer);
            }
        }
    }
}
