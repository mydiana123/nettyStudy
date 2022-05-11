package cn.wjb114514.c1;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {
    public static void main(String[] args) {

        // twr:创建带资源的try
        try (
                FileChannel from = new FileInputStream("a.txt").getChannel();
                FileChannel to = new FileInputStream("b.txt").getChannel()
        ) {
            // transferTo底层使用了 零拷贝优化。三个参数： 开始位置，拷贝多少，拷贝到哪里。
            // 传输上限：Linux最多传2g：改进--多次传输

            // ctrl+alt+v 提取多次使用的变量
            long size = from.size();
            for (long left = size; left > 0; ) {
                // transferTo：返回值为实际传了多少
                // size-left:下次从哪传 left：还剩多少
                left -= from.transferTo((size-left),left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
