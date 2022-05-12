package cn.wjb114514.heimaNetty.Future.Test;

import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 1.获取一个NioEventLoop对象，这个对象内部有一个线程对象。
        group.next();

        // io.netty.util.concurrent.Future;
        Future<Integer> future = group.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("异步线程执行任务并返回执行结果，需要2s");
                Thread.sleep(2000);
                return 70;
            }
        });

        // 2.等待结果[使用get() 仍然是主线程等待future容器里 被放入了异步线程的执行结果]
        // log.debug("等待结果");
        // log.debug("结果是{}",future.get()); // 这句话由主线程打印

        // netty 提供的更强大的异步接受结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                // 此回调方法，只有异步任务执行完毕，才会执行

                log.debug("异步线程执行任务结束~");
            }
        });


        // 相当于 第一次取的时候，异步任务没有执行完，因此future容器里 没有执行结果，而第二次取的时候，异步任务执行完毕，future容器里有相应结果。
        log.debug("我开开心心继续执行~~");
        log.debug("由于异步执行结果需要2s后返回，所以主线程啥也没拿到");
        System.out.println(future.getNow());
        log.debug("气死我了，遇事不决睡大觉。。。 主线程睡5s");
        Thread.sleep(5000);
        log.debug("现在再来取，不信你没有数据！，结果为:{}",future.getNow());
    }

}
