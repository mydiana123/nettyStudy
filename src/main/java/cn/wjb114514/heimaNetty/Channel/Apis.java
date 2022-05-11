package cn.wjb114514.heimaNetty.Channel;

/**
 * close() ==> 关闭channel
 * closeFuture() ==> 处理channel 的关闭事件
 * pipeline() ==> 用于在pipeline这个双向链表节点上，增加处理器
 * write() ==> 数据写入到channel缓冲区
 * writeAndFlush() ==> 写入缓冲区并立刻发出
 *
 * channel.write("hello1\n");
 * channel.writeAndFlush("hello2\n");
 *
 * 服务器 ==> hello1\nhello2\n
 *
 *
 */
public class Apis {
    public static void main(String[] args) {

    }
}
