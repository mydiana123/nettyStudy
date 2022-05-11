package cn.wjb114514.nio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * 缓冲区：缓冲区本质是一个可以读写数据的内存款，可以理解为是一个容器对象，(含数组)，该对象提供了一组方法，可以更轻松的使用内存块，缓冲区对象内置了一些机制
 * 能够跟踪和记录缓冲区的状态变化情况~
 * 可以看到，Buffer对象实际上是对数组对象的一层包装，使得原来只能存储数据的数组对象，有了更强大的功能。
 * Channel提供从文件，网络读取数据的渠道，但是读取和写入数据必须经过Buffer
 *
 * Buffer有 7个子类，除了基本类型的Boolean，都有相对应的buffer
 * 核心存储结构为
 * final float[] hb;                  // Non-null only for heap buffers
 * final int offset;
 * boolean isReadOnly;                 // Valid only for heap buffers
 * 其中7个基本子类下，又有功能更加专一且强大的子类
 * 每个类都继承了Buffer类的四个属性
 * // Invariants: mark <= position <= limit <= capacity
 *     private int mark = -1; 标记，一般不人为修改
 *     private int position = 0; 下一次要操作(读写)的位置
 *     private int limit; 表示缓冲区的当前终点，不能对缓冲区终点外的位置进行操作
 *     private int capacity; 容量，可以容纳的最大数据量，创建缓冲区时决定，不可改变
 *
 * // flip干了啥
 *     public final Buffer flip() {
 *         limit = position;
 *         position = 0;
 *         mark = -1;
 *         return this;
 *     }
 */
public class BufferLearn {
    public static void main(String[] args) {
        // 1.创建一个buffer,指定初始容量，本例可以存放5个数据
        IntBuffer intBuffer = IntBuffer.allocate(5);

        // 2.向buffer里存入数据
        intBuffer.put(10);
        intBuffer.put(11);
        intBuffer.put(12);
        intBuffer.put(13);
        intBuffer.put(14);

        // 3.从buffer里读取数据
        intBuffer.flip(); // 读写切换

        // 设置position属性,即下一个操作的数据是2号位，读取hb数组的hb[2,3,4]
        intBuffer.position(2);
        // 这类方法，传参表示设置，不传参表示返回当前状态，比如
        System.out.println(intBuffer.position());

        // limit可以读取limit或者设置limit
        intBuffer.clear(); // 把各个标记恢复初始状态，并没有真正清除缓冲区
        System.out.println(intBuffer.isReadOnly()); // 是不是只读
        System.out.println(intBuffer.hasArray()); // 是否可以访问缓冲区底层实现的那个数组
        // 类似迭代器的遍历
        while(intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }

        // 最常用的ByteBuffer(网络传输的关键)
        // allocateDirect使用的缓冲区，使用直接内存，也就是操作系统直接分配一块内存给这个缓冲区
        // 而allocate使用的缓冲区，是堆空间里的
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(5);
        // byteBuffer的put和get可以指定读写position位置的数据，而不默认改变position属性

    }
}
