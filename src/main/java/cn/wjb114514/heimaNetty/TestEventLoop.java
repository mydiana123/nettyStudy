package cn.wjb114514.heimaNetty;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * EventLoop [事件循环对象]
 * 本质就是一个 单线程执行器[同时维护了一个Selector]，里面有run方法处理Channel上源源不断的io事件
 * public interface EventLoopGroup extends EventExecutorGroup
 *
 * public interface EventExecutorGroup extends ScheduledExecutorService, Iterable<EventExecutor>
 * 因此EventLoop包含了 线程池里的所有方法 [ScheduledExecutorService]
 * 另外：其继承了netty自己的OrderedEventExecutor ==> 可以有序的执行任务
 * EventLoopGroup [事件循环组]
 * 一组EventLoop Channel一般会调用EventLoopGroup的register方法来绑定给一个EventLoop，后续这个channel上的io事件都有此EventLoop执行
 * 这里应该是观察者模式，即channel为观察者模式的主体，而此channel内部绑定了一个EventLoop作为观察者
 * 观察者时刻 对主体channel 进行观察，当channel上有处理器需要处理事件时，就可以通知观察者来处理
 */

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        /*
        // DefaultEventLoopGroup() ==> 此事件循环组 只负责处理普通任务和定时任务，不处理io事件
        // 无参构造 ==> 如果没传参，则认为传入了线程0，如果发现传入的参数为0，则通过静态变量

        // 1.如果设置了 netty系统属性io.netty.eventLoopThreads，会读取其值
        // 否则默认值为 当前电脑可用CPU核心数 * 2
        // 如果这两个值都不幸 小于1了，就只用一个线程
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
         */
        // 有参构造：获取指定线程的EventLoop工人，也就是2个工人
        EventLoopGroup group = new NioEventLoopGroup(2); // 此事件循环组的功能很强大， 不仅可以处理io事件，还可以处理普通任务，定时任务。
        System.out.println(NettyRuntime.availableProcessors()); // 我们CPU有12核
        // next()方法：循环获取循环对象 [轮询]
        System.out.println(group.next()); // io.netty.channel.nio.NioEventLoop@1b40d5f0
        System.out.println(group.next()); // io.netty.channel.nio.NioEventLoop@ea4a92b
        System.out.println(group.next()); // io.netty.channel.nio.NioEventLoop@1b40d5f0

        // 3.执行普通任务: 可以做一个异步处理。把当前线程的耗时任务 交给其他线程执行
        group.next().submit(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 17:52:53.007 [nioEventLoopGroup-2-1] DEBUG cn.wjb114514.heimaNetty.TestEventLoop - 测试当前事件循环组执行普通任务
            log.debug("测试当前事件循环对象执行普通任务");
        });

        // 4.执行定时任务 [延后/间隔执行某个任务]
        group.next().scheduleAtFixedRate(()->{
            // 两秒后 每隔一秒打印一次
            log.debug("测试当前事件循环对象执行定时任务");
        },2,1, TimeUnit.SECONDS);

        log.debug("我是可爱的主线程~~");
    }

}
