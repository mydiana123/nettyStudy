package cn.wjb114514.heimaNetty.Future.Test;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 其实，future对象是一个很被动的异步对象，
 * 其创建 是向一个线程提交一个又返回结果的run方法[ call() ]后，由此线程提供的，存放执行结果的容器，我们叫做future对象
 * 就好像我们委托快递小哥给我们送快递，但是快递小哥只能送到外卖柜，这里的future对象就是外卖柜
 * 其创建，以及执行结果的存放，都是很被动的，快递小哥只有执行完快递任务后，才能往外卖柜里放数据
 * 而我们只有等待快递小哥执行完任务，才能去future[外卖柜]取数据。
 */
@Slf4j
public class NettyPromise {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1.创建一个执行任务的线程
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2.我们主动的创建一个 可以装执行任务的结果的容器 promise. 容器毕竟是容器，需要一个线程对象来处理任务，promise只负责灵活的存储任务执行的结果
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(()->{
            log.debug("开始计算，计算耗时5s");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 我们主动把此线程的执行结果，放入之前创建的promise容器
           log.debug("5s后，计算完毕，我们把此线程的执行结果放入promise容器");
            // 注意：我们的promise容器也可以 放入失败的执行结果。
            promise.setSuccess(80);
        }).start();

        log.debug("主线程等待promise容器里的数据~~~");
        log.debug("主线程同步阻塞5s后，获得数据:" + promise.get());


        new Thread(()->{
            try {
                int i = 1 / 0;
            }catch (ArithmeticException e){
                e.printStackTrace();
                // 把异常对象存入promise容器
                promise.setFailure(e);
            }
        }).start();

        log.debug("主线程等待异步执行结果~~~ ");
        log.debug("我超，有异常！{}",promise.get());
    }
}
