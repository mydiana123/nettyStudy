package cn.wjb114514.c1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class GatherWrite {
    public static void main(String[] args) {
        ByteBuffer wrap = ByteBuffer.wrap("你好".getBytes(StandardCharsets.UTF_8));
        ByteBuffer wrap1 = ByteBuffer.wrap("世界".getBytes(StandardCharsets.UTF_8));
        ByteBuffer wrap2 = ByteBuffer.wrap("法克".getBytes(StandardCharsets.UTF_8));

        // 集中写
        try(FileChannel channel = new RandomAccessFile("text.txt","rw").getChannel()) {
            channel.write(new ByteBuffer[]{wrap,wrap1,wrap2,wrap});
        }catch (IOException e){

        }
    }
}
