package cn.wjb114514.nio;

import java.io.*;
import java.net.Socket;

/**
 * 零拷贝：是网络编程的关键，很多性能优化离不开零拷贝
 * java里常用的零拷贝有 mmap(内存映射) 和sendFile
 */
public class ZeroCopy_ {
    public static void main(String[] args) throws IOException {
        // 传统方法体现
        File file = new File("demo");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        // 把文件的数据读入到随机访问文件流中，进而存放在bytes数组里
        // 此时，由于发生了文件的读入，用户态切换到内核态，文件数据被读入到内核缓冲区里，采取DMA方式读取(DMA就是不经过CPU的内存访问)
        // 没学操作系统，简单理解就是 DMA的方式的话，在内存里存放了一些类CPU的指令，可以直接完成寻址工作
        // 目前为止，发生了第一次切换，第一次拷贝(文件数据->内核缓冲区)
        // 下一步，内核态切换为用户态，内核缓冲区的数据拷贝到用户缓冲区(也就是我们用来装数据的bytes数组)
        byte[] bytes = new byte[1024];
        raf.read(bytes);

        // 从bytes数组里把文件通过socket传输到其他地方
        // 目前发生了两次切换，两次拷贝
        // 想要完成网络通讯，需要借助硬件资源，比如网卡，网卡想要得到数据，必须经过操作系统
        // 此时用户态转为内核态，操作系统开辟一块用于专门进行网络通讯的缓冲区，并把用户缓冲区的数据读取到网络通讯缓冲区，进行读写 这是CPU拷贝
        // 最后，信息想要被发送出去，需要经过协议栈，最后一次拷贝发生在网络通讯缓冲区(socket buffer) -> protocol stack
        // 四次拷贝，三次切换 硬盘-DMA->内核缓冲区-DMA->用户缓冲区-CPU->网络通讯缓冲区-DMA->协议栈
        // mmap:一种优化方式，可以让用户缓冲区和内核缓冲区共享数据，这样就不需要经过内核缓冲区-->用户缓冲区的拷贝了(3次拷贝，3次切换)
        // sendFile:linux2.1提供的sendFile函数，可以直接从内核缓冲区拷贝到网络通讯缓冲区，不经过用户态
        // 零拷贝是从操作系统角度看的，指的是没有CPU拷贝。
        // 之前的所有方法，都必须经过内核缓冲区到网络通讯缓冲区的拷贝，也就是CPU拷贝

        // Linux2.4 数据可以直接从内核缓冲区拷贝到协议栈，用DMA的方式，仅有少量数据(长度，offset等描述信息)需要经过CPU拷贝到网络通讯缓冲区，可以忽略
        // Hard drive-DMA->Kernel Buffer-CPU(nearly ignore)->[SocketBuffer]-DMA(mainly)->protocol stack
        // 切换次数需要看系统调用的次数，第一次需要读文件，系统调用一次，从user->kernel。
        // 接下来发生的内存拷贝都在内核态完成，拷贝到协议栈后，重新切换到用户态
        // 优点：零拷贝是指内核缓冲区之间没有重复数据(也就是从内核 缓冲区到网络通讯缓冲区的拷贝是重复的)
        // 优势：切换次数-1，更少上下文切换，更少CPU缓存伪共享，无CPU校验和计算
        OutputStream os = new Socket().getOutputStream();
        os.write(bytes);
    }
}
