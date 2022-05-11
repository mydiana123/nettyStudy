package cn.wjb114514.c5;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

@Slf4j
public class AIO {
    public static void main(String[] args) {
        // 参数1：要操作的文件 参数2：对文件的选项 arw之类的，参数3：attach一个线程池。多线程是异步的本质
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("2.txt"), StandardOpenOption.READ)) {
            // 参数1：ByteBuffer
            // 参数2：读取的起始位置
            // 参数3：附件：如果一个buffer一次读不完，可以把上次没读完的buffer附件到此channel
            // 参数4：回调对象
            ByteBuffer allocate = ByteBuffer.allocate(16);
            log.debug("read beginning...");
            channel.read(allocate, 0, allocate, new CompletionHandler<Integer, Buffer>() {

                @Override
                public void completed(Integer result, Buffer attachment) {
                    // 用于异步 等待并复制数据的线程，读取完毕后触发的回调函数。 会把数据写入到attachment里。
                    // result：读取到的实际字节，attachment：存储下一次需要读的数据
                    // 当前用于读取数据的线程Thread-12
                    System.out.println("当前用于读取数据的线程" + Thread.currentThread().getName());
                    attachment.flip();
                    log.debug("read completed...");
                    debugAll((ByteBuffer) attachment);

                }

                @Override
                public void failed(Throwable exc, Buffer attachment) {
                    // 读取失败的操作
                    exc.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("read end...");
        // 如果不让主线程睡一会，我们读取数据的线程需要一定事件读取数据，用于读取数据的线程还在那读呢，主线程先结束了，那读取数据的线程就跟着主线程一起死了
        // 而把睡觉的代码去掉，则会发现。确确实实主线程没有因为IO事件而阻塞，而且主线程获取读取数据也是 依靠了Thread-12这个线程的回调函数完成的
        // 所以在AIO里，主线程在发生IO事件时，既不用等待是否有可以读的数据，也不用等待数据从网卡读到内存的过程，全都交给工具人线程来干~~
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
