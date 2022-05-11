package cn.wjb114514.nio;

/**
 * SelectionKey可以认为是 Selector用于管理Channel的一个工具
 * 我们使用Selector的open()方法，获得的Selector的 运行类型是WindowsSelectorImpl
 * keys() 方法：返回所有注册到Selector的通道数
 * selectedKeys()方法：返回当前有事件发生的通道数 [也就是当前需要处理的方法，如果我们从迭代器里移除一个处理完的方法，这个数量就会对应减少！]
 * OP_READ = 1
 * OP_WRITE = 4
 * OP_CONNECT = 8
 * OP_ACCEPT = 16
 *
 * selector() : 通过selectionKey获得Selector对象
 * channel() : 通过selectionKey获得Channel对象
 * 可以看到，SelectionKey维护一个Selector -> Channel 的映射关系
 * attachment() : 通过selectionKey获得当前Channel 的共享数据，一般都是Buffer数据。
 * 可以看到，SelectionKey 也维护了Channel -> Buffer 的 数据流通
 * interestOps() : 修改当前Selector监听的事件类型
 *
 * 下面的方法用于区分当前Selector监听Channel 的哪些事件，当发生Channel事件是，要判断与之关联的Selector有没有处理该事件的能力
 * 比如ChannelA发生了Read事件，如果selector无法处理Read事件，那也是于事无补的~
 * isAcceptable()
 * isReadable()
 * isWritable()
 */
public class SelectionKeyAPI {
}
