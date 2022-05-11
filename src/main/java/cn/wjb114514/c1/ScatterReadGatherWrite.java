package cn.wjb114514.c1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.RandomAccess;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

/**
 * 分散读取，集中写入
 */
public class ScatterReadGatherWrite {
    public static void main(String[] args) {
        // RandomAccessFile() 可以不在编译期指定究竟是 输出流和输出流，而在构造器里指定，较为灵活获取channel
        try (FileChannel channel = new RandomAccessFile("a.txt","r").getChannel()){
            // 演示数据的分散读取 ：读取到多个buffer
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer3 = ByteBuffer.allocate(5);
            ByteBuffer byteBuffer4 = ByteBuffer.allocate(7);

            // 分散读取
            channel.read(new ByteBuffer[]{byteBuffer1,byteBuffer2,byteBuffer3,byteBuffer4});
            debugAll(byteBuffer1);
            debugAll(byteBuffer2);
            debugAll(byteBuffer3);
            debugAll(byteBuffer4);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
