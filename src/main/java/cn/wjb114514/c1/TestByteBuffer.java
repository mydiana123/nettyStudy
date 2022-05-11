package cn.wjb114514.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        // 获得一个FileChannel
        // 获取方式： 1.使用输入输入流 2.通过随机读写文件获取
        try(FileChannel channel = new FileInputStream("a.txt").getChannel()){
            // 使用带资源的try，自动关闭相关资源

            while (true) {
                // 1.准备缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(10);

                // 2.从channel中读取数据，写入到buffer
                int read = channel.read(buffer);  // 返回值为-1表示读到eof，没内容了
                log.debug("读取到的字节数{}",read);
                if (read == -1) break;
                // 3.转换buffer的读写模式[从写模式转换为读模式]
                buffer.flip();

                while (buffer.hasRemaining()) {
                    // 每次读取一个字节
                    // 空参值会读取当前pos 的数据 并自动把pos+1。
                    byte b = buffer.get();
                    log.debug("读取到的字符{}",(char) b);
                    System.out.println((char) b);
                }
                // 不加这句话会导致pos和limit来回换，死循环
                buffer.clear();
            }

        }catch (IOException e){

        }
    }
}
