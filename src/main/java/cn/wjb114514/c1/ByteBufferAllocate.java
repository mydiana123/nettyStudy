package cn.wjb114514.c1;

import java.nio.ByteBuffer;

public class ByteBufferAllocate {
    public static void main(String[] args) {

        // class java.nio.HeapByteBuffer java堆内存，读写效率低，受到GC影响[可能会受到gc，而数据移动，进行数据搬迁。]
        // class java.nio.DirectByteBuffer 直接内存，os直接分配，读写效率高，少一次拷贝[相当于mmap内存映射，直接把直接内存和缓冲区映射]，不受到gc影响
        System.out.println(ByteBuffer.allocate(16).getClass());
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
        // 缺点：1.直接内存需要经过操作系统函数，分配效率低。2.由于不受到GC，需要程序员手动释放，如果释放不当， 会造成内存泄漏


    }
}
