package cn.wjb114514.nio;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * 通道类似于流，可以看做一个连接
 * 1.通道可以同时读写，而流只能进行读或者写
 * 2.通道可以实现异步读写数据
 * 3.Channel和Buffer的数据流通也是双向的
 *
 * Channel是一个接口，可以看做是一个规范
 * A channel represents an open connection to an entity such as a hardware device,
 * a file, a network socket, or a program component that is capable of performing one or more distinct I/O operations,
 * for example reading or writing.
 * ctrl+h查看继承关系，其重要子类为:FileChannel,DatagramChannel(udp),ServerSocketChannel(Server),SocketChannel(client)[tcp]
 * Server端有一个ServerSocketChannelImpl对象。主线程通过此对象，为每个客户端线程创建一个SocketChannelImpl对象
 * 可以认为SocketChannelImpl对象实现了服务器端线程和客户端线程的通讯
 * FileChannelImpl的常用方法:write(Buffer写入Channel) read(Buffer读取Channel)[方法作用的对象是Channel] transferForm(从目标通道拷贝数据到当前通道) transferTo
 */
public class ChannelLearn {
    public static void main(String[] args) throws IOException {
        String str = "hello,world!";
        // 1.创建一个原生java输出流，并包装为Channel对象
        FileOutputStream fos = new FileOutputStream("g:\\1.txt");
        // FileOutputStream包装了一个FileChannel
        FileChannel fileChannel = fos.getChannel(); // 此fileChannel的运行类型为FileChannelImpl

        // 2.创建一个缓冲区Buffer，与Channel进行数据交互
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // 3.将str放入到Buffer里
        byteBuffer.put(str.getBytes(StandardCharsets.UTF_8));

        // 4.反转Buffer，从写变读 byteBuffer[pos=12,lim=1024,cap=1024] => [pos=0,lim=12,cap=1024]
        byteBuffer.flip();

        // 5.Buffer写入Channel(Channel读取Buffer数据，Buffer从写模式要变成读模式)
        fileChannel.write(byteBuffer);
        fileChannel.close();
    }
    @Test
    public void ReadFromFileByChannel() throws IOException {

        // 1.创建文件输入流
        File file = new File("g:\\1.txt");
        FileInputStream fis = new FileInputStream(file);

        // 2.通过输入流获取文件Channel
        FileChannel fileChannel = fis.getChannel();

        // 3.创建Buffer对象
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        // 4.读取Channel数据，写入Buffer
        fileChannel.read(byteBuffer);

        // 5.将字节数组转为字符串，并打印在stdout
        System.out.println(new String(byteBuffer.array()));
    }
    @Test
    public void FileCopyByChannel() throws Exception{

        // 1.获得文件输入输出流对应的Channel
        FileInputStream fileInputStream = new FileInputStream("g:\\1.txt");
        FileChannel fileChannel01 = fileInputStream.getChannel();
        // File对象的根目录是项目目录
        FileOutputStream fos = new FileOutputStream("2.txt");
        FileChannel fileChannel02 = fos.getChannel();

        // 2.生成一个中转站Buffer[使用allocate分配的Buffer，运行类型是HeapByteBuffer，也就是分配在堆空间上的]
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        // 3.从管道中读取数据,写入Buffer read的对象是Channel，所以用read方法
        while(true) {
            // 这句话是很重要的，因为我们读写都操作一个缓冲区，那么缓冲区读取一些，就写一些，就达到一个平衡状态了
            // 比如一个文件就20bytes，按理说第一次读到文件末尾的前一个bytes，返回20，第二次读末尾，返回-1
            // 读20个bytes，写20个bytes，读的时候，pos=20,limit=1024 clip之后pos=0,limit=20,写的时候pos=20,limit=20
            // 之后我们就发现，读不进去了，因为写入之后pos=limit，不让你读了，那个文件末尾就永远也读不到了
            // 所以我们每次写完后，要把缓冲区clear，就像拉完屎要擦屁股
            byteBuffer.clear();
            int read = fileChannel01.read(byteBuffer);
            if (read == -1) break;
            // 4.每读取一些，就写一些
            byteBuffer.flip();
            fileChannel02.write(byteBuffer);
        }
        // fileChannel01.close();
        // fileChannel02.close();
        fos.close();
        fileInputStream.close();

    }
    @Test
    public void CopyImgByChannel() throws IOException {

        // 1.创建输入输出流和相关管道
        FileChannel dst = new FileOutputStream("g:\\img2.png").getChannel();
        FileChannel src = new FileInputStream("g:\\img.png").getChannel();

        // 2.把数据直接从一个管道转移到另一个管道
        dst.transferFrom(src,0,1024000);

        dst.close();
        src.close();
    }
}
