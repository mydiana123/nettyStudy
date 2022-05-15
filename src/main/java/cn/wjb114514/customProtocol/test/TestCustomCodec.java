package cn.wjb114514.customProtocol.test;

import cn.wjb114514.customProtocol.message.LoginRequestMessage;
import cn.wjb114514.customProtocol.message.Message;
import cn.wjb114514.customProtocol.protocol.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

public class TestCustomCodec {
    public static void main(String[] args) throws Exception {
        /*
        把这个帧解码器抽取出来，供多个channel使用
        问题 ==>
        此handler会被多个EventLoop使用 [EventLoop是工人，handler是工序]
        工人1 使用了FRAME_DECODER  ==> channel1发生了半包， 工人1记录了当前半包的数据 data1。工人1 继续等待数据 ...
        工人2 使用了同一个FRAME_DECODER ==> 工人二正在处理channel2的半包事件，于是从解码器里拿出半包数据，发现data1，也拼在一起了。
        发送给服务器的数据 是 channel1和 channel2 的拼接数据 ！！
        因此 ==> handler存在线程安全问题。
        但是，有一些handler没有状态信息。比如日志handler，来什么打印什么 ==> 无状态的handler，可以被多线程使用
        @Sharable 注解 ==> 这就是一个空注解，但是可以被获取，获取到此注解的话，就认为此handler是 线程安全的.
        因此，netty内置的handler，加入了@Sharable 就可以放心的在多线程使用
        一般而言，解码器性质的handler，都是设计数据保存和访问的，在多线程下容易出现线程安全问题。
         */
        LengthFieldBasedFrameDecoder FRAME_DECODER = new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0);
        EmbeddedChannel ch = new EmbeddedChannel(
                new LoggingHandler(),
                // 定义一个帧解码器。 最大帧 ==> 我们指定为1024，第12位才表示长度位，长度字节为4byte，长度和内容之间没有附加信息，不需要丢弃任何内容
                FRAME_DECODER,
                new MessageCodec()
        );

        // writeOutbound ==> 从服务器往客户端写 writeInBound ==> 从客户端往服务器写
        // readInbound ==> 服务器端读客户端内容[有数据in服务器，所以读in就是服务器读]。 readOutBound ==> 客户端读服务器端内容
        // In和Out的基准都基于服务器端，即in就是 有数据in进入服务器，out就是有数据从服务器out
        // 读in ==> 服务器读， 读out ==> 客户端读
        // 写in ==> 客户端写[因为有数据流入服务器，所以一定是客户写了，才有数据流入服务器]
        ch.writeOutbound(new LoginRequestMessage("zhangsan", "123"));

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        Message message = new LoginRequestMessage("wangwu", "114514");
        new MessageCodec().encode(null, message, buf);

        // 客户端把 编码后的数据写入服务器，经过解码入站处理器
        // ch.writeInbound(buf);

        // 问题: 半包 ==> 缓冲区的剩余内容，不够读取一条ByteBuf的全部内容，导致反序列化数据丢失，进而失败。 ==> 解决，使用帧解码器

        // 魔数为 16909060 我们采取大端法，魔数就是 01020304 ==> 很大的一个数.

        // 实验 : 演示没有解码器导致半包现象 ==> 使用切片

        // 把当前buf切为 两片 注意:调用writeInbound() 会调用buf的release，会导致切片无法使用的问题，需要我们手动retain()
        ByteBuf buf1 = buf.slice(0, 100);
        ByteBuf buf2 = buf.slice(100, buf.readableBytes() - 100);
        ch.writeInbound(buf1);

        /*
                if (checkBounds && readerIndex > writerIndex - minimumReadableBytes) {
            throw new IndexOutOfBoundsException(String.format(
                    "readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s",
                    readerIndex, minimumReadableBytes, writerIndex, this));
        }
         */
        // 报错原理是，这个checkBounds应该是 检查是否存在越界问题的开关，如果不指定 帧解码器，会按照默认方式解码
        // 而默认方式会对ByteBuf进行检查 ==> 我们在解码器里，一次要求读取 in.readBytes(data,0,contentLength);
        // 这个方法 只有把contentLength的数据都读完了，才可以移动read index。
        // 于是，如果 read index > write index - readableBytes。就说明，这个方法还没读完，就被意外终止了[半包] ==> 报异常
        // 而引入了帧解码器后，可能会修改底层的逻辑。就是认为还存在下一次读取，暂时不报异常，等到下一次读取完了[半包的下一个] ==> 发现正常，就继续读下去
    }


}
