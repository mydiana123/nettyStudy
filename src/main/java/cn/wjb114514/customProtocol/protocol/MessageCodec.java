package cn.wjb114514.customProtocol.protocol;

import cn.wjb114514.customProtocol.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 自定义协议的 编码-解码器
 * ByteToMessageDecoder<T>  ==> 规定了把泛型为T类型的对象，解码为byte[]数组的做法。
 * Codec ==> 既包含解码器，又包含编码器
 */
@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {

    /**
     *
     * @param ctx
     * @param msg
     * @param out netty创建好的ByteBuf，只需要把我们编码后的消息，写入out即可
     * @throws Exception
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 出站前，用于编码

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
    }

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
