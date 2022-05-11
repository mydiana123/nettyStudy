package cn.wjb114514.nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NewIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1",6667));

        long start = System.currentTimeMillis();
        // 注意：在Linux下，一次transferTo可以传输任意大小的文件，但是Windows下，一次只能传8M。transferTo方法底层就使用了零拷贝
        FileChannel fileChannel = new FileInputStream("C:\\Users\\DELL\\Desktop\\wxk1991.7z").getChannel();
        /*
        This method is potentially much more efficient than a simple loop that reads from this channel
         and writes to the target channel. Many operating systems can transfer bytes directly
         from the filesystem cache to the target channel without actually copying them.
         */
        long transferCount = fileChannel.transferTo(0,fileChannel.size(),socketChannel);
        long end = System.currentTimeMillis();
        System.out.println("花费时间:" + (end - start) + "ms");

    }
}
