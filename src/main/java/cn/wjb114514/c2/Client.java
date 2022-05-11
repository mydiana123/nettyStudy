package cn.wjb114514.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * debug的Evaluate Expression功能很有意思，相当于可以在被调试的一方 凭空执行一句代码~~~
 */
public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",1145));
        sc.write(ByteBuffer.wrap("hello，world！".getBytes(StandardCharsets.UTF_8)));
        System.out.println("waiting");
    }
}
