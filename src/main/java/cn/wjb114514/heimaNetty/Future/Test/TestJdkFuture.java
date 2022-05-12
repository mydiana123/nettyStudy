package cn.wjb114514.heimaNetty.Future.Test;

import java.util.concurrent.*;

public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1.jdk的future多基于线程池
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 2.提交任务
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                // 我们假设工作线程睡了1s才返回结果
                System.out.println("我是苦逼的异步线程" + Thread.currentThread().getName() + "专门帮主线程执行任务 555 ~");
                Thread.sleep(1000);
                return 50;
            }
        });

        // 其实future 就是线程间传递数据的容器，而future只能被动获取容器内容，即只有异步线程 执行完任务后，才能把任务结果放回到Future里。
        // 而主线程会阻塞，直到future容器里有数据可读，才继续，就相当于主线程卡着，等待异步线程把执行结果放入future
        // 3.主线程通过 异步对象future获取结果
        System.out.println("本线程" + Thread.currentThread().getName() + "同步阻塞，等待异步线程返回结果给我~~");
        // 执行get()方法后，主线程进入休眠 会等待 执行任务的异步线程发来一个 中断信号，解除阻塞。
        System.out.println("委托给异步线程执行的结果为:" + future.get());
        System.out.println("本线程" + Thread.currentThread().getName() + "执行完了~~");
    }
}
