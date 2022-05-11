package cn.wjb114514.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class BufferScatterAndGather{
    /**
     * 懵逼了，为什么测试方法里就跑不起来??
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println(Thread.currentThread().getName());
        // 1.获取一个serverSocketChannel，并绑定端口到channel关联的socket上
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(7777);
        serverSocketChannel.socket().bind(inetSocketAddress);


        // 2.创建buffer数组
        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(500);
        byteBuffers[1] = ByteBuffer.allocate(300);

        // 3.服务器端监听 等待客户端的连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        // 4.循环读取
        int messageLength = 800;
        while (true) {
            int byteRead = 0;
            while (byteRead < messageLength) {
                // 从客户端的socket处读取数据
                long l = socketChannel.read(byteBuffers);
                byteRead += l;
                System.out.println("byteRead=" + byteRead);
                // 使用流打印，查看当前buffer的position和limit
                Arrays.stream(byteBuffers).map(buffer -> "position=" + buffer.position()
                        + ";limit=" + buffer.position()).forEach(System.out::println);
                // System.out.println(byteBuffers[0].get());
                // 将所有的buffer进行flip
            }


            Arrays.asList(byteBuffers).forEach(Buffer::flip);

            // 将数据读出，显示到客户端
            long byteWrite = 0;
            while (byteWrite < messageLength) {
                // 向客户端socket通道写入数据
                long l = socketChannel.write(byteBuffers);
                byteWrite += l;
            }

            // 写完之后要clear，要不然会循环不断的把相同的内容写回去
            Arrays.asList(byteBuffers).forEach(Buffer::clear);
            System.out.println("byteRead=" + byteRead + ";byteWrite=" + byteRead + ";messageLength=" + messageLength);
        }
    }
}
