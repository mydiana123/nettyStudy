package cn.wjb114514.customProtocol.protocol;

import cn.wjb114514.customProtocol.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 这个父亲 需要自定义 编解码器转换信息的双方类型
 * 并且要保证，此处理器之前，必须经过黏包半包处理器，否则会导致线程安全问题。
 * 无论什么情况，in都表示服务器有数据了，入站就表示因为客户端写入导致服务器有数据，出站就表示因为服务器写出导致有数据
 * 泛型一 表示入站的in，即客户端以 泛型1的数据类型 把数据发给服务器
 * 泛型二 表示出站的in，即服务器以 泛型2的数据类型 把数据发给客户端
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf,Message> {

    // 可用于多条消息的编码
    // 把Message封装到ByteBuf，也就是说，服务器要想把Message写出，必须包装为ByteBuf对象
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        // 出站前，用于编码
        ByteBuf out = ctx.alloc().buffer();
        // 1. 四个字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});

        // 2. 一个字节的版本
        out.writeByte(1);

        // 3. 一个字节的序列化算法 ==> jdk [以后可以支持多种序列化算法，比如0代表jdk，1代表json]
        out.writeByte(0);

        // 4.消息类型 [我们要处理的消息对象，提供了获取消息类型的方法]
        out.writeByte(msg.getMessageType());

        // 5.请求序号 4bytes
        out.writeInt(msg.getSequenceId());

        // 无意义的字节，用于padding[对齐填充] 我们要让我们的报头部分尽量是2的整数倍。如果不是 要对齐
        out.writeByte(0xFF);

        // 6.正文长度: 正文对象是一个对象，我们需要序列化后才能进行网络传输。
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos); // 把对象的二进制字节码流 写入字节输出流
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();

        out.writeInt(bytes.length);

        // 7.消息正文
        out.writeBytes(bytes);

        outList.add(out);
    }

    // 由于客户端发来的数据是ByteBuf，服务器要先解码读取内部的数据
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 入站后，用于解码 [反着来]
        int magicNum = in.readInt();// 读4个字节，拿到魔数
        byte version = in.readByte(); // 版本
        byte serializeType = in.readByte(); // 序列化方式
        byte messageType = in.readByte(); // 消息类型
        int seqId = in.readInt(); // 消息的序列号
        in.readByte(); // 跳过填充字符
        int contentLength = in.readInt(); // 正文长度

        byte[] data = new byte[contentLength]; // 接受数据的缓冲数组
        in.readBytes(data,0,contentLength);

        // 把代表对象的二进制流数组 反序列化为一个message对象
        Message msg = null;

        if (serializeType == 0) {
            // 如果消息采取了 jdk的序列化
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            msg = (Message) ois.readObject();
        }
        log.debug("魔数==>{};版本==>{};序列化方式==>{};消息类型==>{},消息序列号==>{}",magicNum,version,serializeType,messageType,seqId);
        log.debug("正文长度==>{};消息==>{}",contentLength,msg);

        // 解码后的结果要装入out，因为下一个入站处理器获取的是out对象
        out.add(msg);
    }
}
