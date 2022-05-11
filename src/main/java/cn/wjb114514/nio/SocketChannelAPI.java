package cn.wjb114514.nio;

/**
 * 获取一个socketChannel
 * open()
 * 获取当前channel 的socket对象
 * socket()
 *
 * 把当前的socket与端口和ip进行绑定
 * bind()
 *
 * 进行监听，等待连接，在NIO里，此方法是阻塞的，不过只有当已经确定有客户端连接时才会执行此方法，因此不会陷入长久的阻塞，也就是傻等。
 * accept()
 *
 * SocketChannel和serverSocketChannel都继承于AbstractSelectableChannel(这里的Selectable可以认为这些通道的事件能被Selector选择)
 * SocketChannel实现了Scatter和GatherByteChannel 所以socketChannel更注重数据的读写。这里的分散和聚集就是能对buffer数组灵活读写
 * 而serverSocketChannel更注重连接的管理
 *
 * 客户端channel连接服务器的方法，此方法是阻塞的，不过在NIO里，不会一直阻塞，如果发现无法连接服务器，会去干别的事情
 * connect()
 *
 * 如果上面的方法连接失败，接下来就通过此方法完成连接操作
 * finishConnect()
 *
 * 注册 selector - channel - buffer 三元组 ，用selectionKey管理
 * register
 *
 *
 */
public class SocketChannelAPI {
}
