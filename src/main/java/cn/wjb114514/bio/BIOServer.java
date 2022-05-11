package cn.wjb114514.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用线程池机制，模拟BIO服务器
 */
public class BIOServer {

    // 1.创建一个线程池
    // 2.如果有客户端连接，就创建一个线程与之通讯。
    public static void main(String[] args) throws Exception {
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("服务器启动了");
        while (true) {

            // 监听，等待客户端连接.连接成功则获取用于和客户端通讯的socket对象
            System.out.println("服务器阻塞在监听处~！！");
            final Socket socket = serverSocket.accept();
            System.out.println("连接到一个客户端");

            // 连接成功就创建一个线程，与之通讯
            newCachedThreadPool.execute(() -> {
                // 打印服务器端启动的线程信息
                System.out.println("线程名字:"+Thread.currentThread().getName());
                // 实现Runnable接口的run方法，实现和客户端的通讯
                handler(socket);
            });
        }
    }

    // 编写一个通用的和客户端通讯的方法
    public static void handler(Socket socket) {
        try {
            byte[] bytes = new byte[1024];
            // 通过socket获取输入流
            InputStream inputStream = socket.getInputStream();
            // 循环读取client发送来的数据
            while(true) {
                // BIO：会阻塞直到读写操作完毕，只要读写还在继续，就算没有数据也会阻塞，等待读取
                // NIO：把读写操作视为一个事件，就算读写操作没有完毕，也只会在读写事件发生时处理，其他时间完全可以干别的操作！
                System.out.println("服务器线程阻塞在等待读写客户端数据处！！其他操作只有等到阻塞结束后才能被执行！！");
                int read = inputStream.read(bytes);
                // 管道缓冲区仍有数据，继续读取
                if (read != -1) {
                    // 输出读取到客户端的数据
                    System.out.println(new String(bytes,0,read));
                }else {
                    // 结束此次通讯
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("关闭和client的连接");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
