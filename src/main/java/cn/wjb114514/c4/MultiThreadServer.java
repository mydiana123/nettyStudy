package cn.wjb114514.c4;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.wjb114514.c1.ByteBufferUtil.debugAll;

/*
// 主要问题就是，由于selector在worker-0线程循环阻塞监听，会导致worker-0卡住，进而无法处理 客户端读事件，而且由于selector被阻塞，导致boss线程想要把客户端channel注册进去都注册不了。只有客户端的读事件被select()监听到了，才会使selector进入非阻塞，这样才能把客户端channel注册。这是很笨重的
解决办法就是 ==> 每次有一个客户端连接请求来了，boss线程使用wakeup()唤醒selector，使其非阻塞，之后再把客户端channel注册进来。注册完了selector在继续执行自己的select（）进行监听
 */
// 我们理解key时，就把他认为是 Selector-Channel-Buffer的三元组即可
@Slf4j
public class MultiThreadServer {

    // 工人也是一个线程
    static class Worker implements Runnable {
        private Thread thread; // 处理IO事件的线程
        private Selector selector; // 监听IO事件的selector
        // 消息队列 可以让代码有序执行
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private String name;
        // volatile为了保证多线程下的一致性,可见性
        // 也就是如果一个线程修改了volatile变量，此修改操作会立即通知其他线程，这样其他线程可以立马嗅探到此线程对此变量的操作
        // 并认为自己的缓存中的此变量为失效，并修改
        private volatile boolean started = false; // 判断此worker是否被初始化，我们要保证worker只被初始化一次，因为一个worker对应一个线程

        public Worker(String name) {
            this.name = name;
        }

        // 初始化代码
        public void register(SocketChannel sc) throws IOException {
            if (!started) {
                selector = Selector.open();
                thread = new Thread(this, name);
                thread.start();
                started = true;
            }
            // 加入一个任务，但是任务没被执行[没调用线程的start方法，单纯创建一个线程对象]
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            // 每次主线程把一个任务对象交给worker-0线程，就会先把selector唤醒，这样worker-0的run方法就会继续执行
            // 然后worker-0的run方法就把任务对象进行执行，完成sc.register() 然后再次进入循环，selector()继续阻塞worker-0监听...
            selector.wakeup();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // 这个方法让 选择器selector工作在worker-0线程 [使得worker-0阻塞]
                    Runnable task = queue.poll();
                    if (task != null) {
                        // 这里的run方法只有等到selector非阻塞后才能执行 [因为run方法代表了sc.register()方法]
                        task.run(); // 原本被boss线程执行的run方法，现在由worker-0来执行。
                        // 这就相当于主线程让注册工作[此工作就是一个线程对象] 先入队，排队，等着别人来处理，这下worker线程来处理了
                        // 相当于主线程通过任务队列，把任务对象交给了别的线程来处理，这正是我们的目的~
                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        // 只需处理读写事件[当前的key关联的channel是客户端的]
                        // 因此一个客户端的key可读，意味着服务器向客户端的channel里写入了数据
                        if (key.isReadable()) {

                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("before read...{}", channel.getRemoteAddress());
                            channel.read(buffer);
                            log.debug("after read...{}", channel.getRemoteAddress());
                            buffer.flip();
                            debugAll(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT); // bossChannel只关注accept事件
        ssc.bind(new InetSocketAddress(1145));
        // 创建一个worker处理此客户端channel的读写事件
        // 一个Worker可以管理多个channel的读写事件

        // 改进方式2 ==> 多个worker
        // Worker worker = new Worker("worker-0");
        AtomicInteger index = new AtomicInteger();
        // Runtime.getRuntime().availableProcessors()
        // bug: 在docker容器中，java会直接获取物理机中cpu核心数，比如物理机八核，但是只给docker容器分配1核，那么获取的还是8核
        // 直到jdk10+，在jvm参数里 指定+UserContainerSupport[默认开启] 才会考虑到容器的真实cpu数
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for(int i = 0; i < workers.length ; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove(); // 从SelectedKeys里移除当前的key对象~
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected...{}", sc.getRemoteAddress());
                    // 把读写事件关联到worker的selector
                    log.debug("before register...{}", sc.getRemoteAddress());
                    /*
                    --------- 实际上，boss线程执行到这里就被卡住了
                    问题：由于我们在worker.register时，开辟了一个线程，并且注册了一个选择器selector
                    而，selector正在worker开辟的线程中进行循环监听，其处在阻塞状态
                    这就导致这里的sc.register(worker.selector)要用到selector，但是
                    selector还在worker线程里阻塞呢，也就是说，除非selector进入非阻塞状态
                    否则sc.register()会一直卡着，直到selector处于非阻塞。
                     */
                    /*
                    // 如果我们把worker-0线程的穿创建和boss线程里的很近
                    // 就有可能先完成sc.register()的创建任务，后创建worker-0
                    // 就不会出现我们之前讨论的情况

                    // 因此，我们必须要让 sc.register(worker.selector) 也就是把当前通道注册到worker 的selector上
                    // 必须先于 worker.register() 也就是开辟一个线程 让worker.selector() 循环阻塞监听 的工作
                     */
                    /*
                    问题2：
                    如果这时候worker.selector已经创建好了，并且已经在worker-0线程里跑了。
                    但是如果我们又来了一个客户端，新的客户端一想连接，发现需要用到的selector又被卡住了....
                    解决方案：
                    netty的做法
                    Thread    Selector[两个线程共用一个selector]
                    worker-0      . [阻塞] ... 如果selector在worker-0里阻塞。就会导致boss线程无法使用此selector
                    boss          .
                    解决方式： 让selector尽量在一个线程中工作
                    解决方式，把sc.register()方法，放到worker线程的初始化工作里。
                    问题又来了：由于run方法启动后，才会导致selector循环监听卡住。
                    此时selector工作在worker线程
                    而实际调用sc.register()的线程仍然是boss线程，所以依旧是两个线程 争夺selector使用权...
                     */
                    /*
                    解决方法2：
                    直接使用wakeup()方法，舍弃任务队列
                    也就是说：
                    1.boss线程 ==> 唤醒worker-0的selector [boss] selector.wakeup()
                    2.boss线程 ==> 注册客户端sc到selector [boss] sc.register(selector)
                    3.worker线程 ==> 被唤醒后继续工作，之后再次执行到select()方法，阻塞 [worker] selector.select()
                    执行顺序：
                   c1. 每当有一个客户端来注册，就会按照 3->1->2->3->1->2 的方式进行，因此可以达到和我们之前的做法相同的目的
                   c2. 如果按照 1->3->2 的方式运行，也就是 先wakeup() 之后，selector才开始阻塞监听。但是由于提前收到了wakeup()的信号
                    所以其实和c1一样
                   c3. 其实和c2一样，进而得到和c1一样的情况。
                     */
                    // 这里怎么把客户端channel均匀的绑定到 worker线程里，因为目前有多个worker线程
                    // 采取负载均衡算法 round-robin
                    // worker.register(sc); // 记得要把worker初始化==>开辟一个worker-0线程，内部有一个selector循环阻塞监听

                    // 第一个连接取 worker0 第二个就就 worker1 第三个 worker0
                    // ... 循环轮询
                    // 为了充分发挥cpu的核数，所以可以把worker的线程数 至少设置为CPU核数
                    workers[index.getAndIncrement() % workers.length].register(sc);

                    log.debug("after register...{}", sc.getRemoteAddress());
                }
            }

        }


    }
}
