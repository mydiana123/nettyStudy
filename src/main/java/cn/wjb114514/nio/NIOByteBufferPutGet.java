package cn.wjb114514.nio;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class NIOByteBufferPutGet {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);
        // 1.按照类型化方式放入数据
        byteBuffer.putInt(100);
        byteBuffer.putDouble(9.0);
        byteBuffer.putChar('@');

        // 2.取出：怎么放怎么去取，就像序列化一样，必须按照存入的顺序取出
        byteBuffer.flip();

        System.out.println(byteBuffer.getInt());
        // 这里涉及到了强转 输出了4621256167635550208
        System.out.println(byteBuffer.getLong());
        // BufferUnderflowException 这是因为Char占一个槽位（4bytes），Double占2个slot(8bytes) 超出内存
        System.out.println(byteBuffer.getDouble());
    }

    @Test
    public void ReadOnlyBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);
        for (int i = 0; i < 64; i++) {
            byteBuffer.put((byte) i);
        }
        byteBuffer.flip();

        // 这里返回的buffer应该是类似于集合的视图对象~
        ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();

        // java.nio.HeapByteBufferR
        System.out.println(readOnlyBuffer.getClass().getName());

        // ReadOnlyBufferException
        // readOnlyBuffer.putInt(20);
        while (readOnlyBuffer.hasRemaining()) {
            System.out.println(readOnlyBuffer.get());
        }
    }

    @Test
    public void MappedBuffer() throws IOException {
        // MappedByteBuffer 可以让文件直接在内存[堆外内存，也就是直接内存]里修改，即操作系统不需要拷贝一次
        // MappedByteBuffer的运行类型是DirectByteBuffer 所以可以直接使用 直接内存修改

        // 1.获取一个可随机访问的文件，并指定权限
        RandomAccessFile randomAccessFile = new RandomAccessFile("2.txt", "rw");

        // 2.获得和此文件关联的管道
        FileChannel channel = randomAccessFile.getChannel();

        // 3.设置管道参数: 参数1表示权限，我们指定为读写 参数2 position表示可以修改的起始位置 参数size表示把文件从position到position+size这么多字节映射到内存
        // 可以直接修改的范围，就是[position,position+size) 如果越界：抛出数组越界异常
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);

        map.put(1, (byte) 'H');

        randomAccessFile.close();
    }


    // Buffer的scatter和gather
    @Test
    /*
        Scattering 表示将数据写入到buffer时，可以采取buffer数组，依次写入 [分散]
        Gathering 表示从buffer读取数据时，可以从多个buffer数组里读取，并聚合
        有点类似tcp发送端把数据包拆分成很多包，这些包在接收端合并
     */
    public void bufferScatterAndGather() throws IOException {

    }
}
