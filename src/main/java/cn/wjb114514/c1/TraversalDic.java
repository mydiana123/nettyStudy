package cn.wjb114514.c1;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

// 遍历文件夹的API
public class TraversalDic {
    public static void main(String[] args) throws IOException {

        long s = System.currentTimeMillis();
        // 匿名内部类引用外部局部变量，相当于外部变量是final的。因为java设计者害怕多线程对外部变量的影响

        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();

        // java体现事件回调函数的机制，就是引入一个lambda表达式/匿名内部类，在监听到相关事件后，自动调用匿名内部类[即我们实现接口的具体实现类]里的相关方法

        // 此方法使用了访问者模式
        Files.walkFileTree(Paths.get("C:\\Users\\DELL\\IdeaProjects"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 遍历目录前的操作。
                System.out.println("当前目录为====>" + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);

            }

            // 实际目录数量比遍历到的少一，是因为windows属性查看时，不包括自身目录
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 遍历到文件时 的操作
                System.out.println("当前文件为====>" + file.toFile().getName());
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("当前文件数量为：" + fileCount);
        System.out.println("当前目录数量为：" + dirCount);

        System.out.println("使用时间" + (System.currentTimeMillis() - s));
    }

    @Test
    public void getJarsOfCurrentPath() throws IOException {
        // 检查此路径下的所有jar包
        long s = System.currentTimeMillis();
        // 匿名内部类引用外部局部变量，相当于外部变量是final的。因为java设计者害怕多线程对外部变量的影响

        AtomicInteger jars = new AtomicInteger();

        Files.walkFileTree(Paths.get("C:\\Users\\DELL\\IdeaProjects"), new SimpleFileVisitor<Path>() {


            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".jar")) {
                    jars.incrementAndGet();
                    System.out.println("当前jar包为====>" + file.toFile().getName());
                }
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("当前目录下有：" + jars + "个jar包");
        System.out.println("使用时间" + (System.currentTimeMillis() - s));
    }

    @Test
    public void deleteMultiplyDirectory() throws IOException {
        // 删除多级目录:用程序删除是非常危险的，轻易不要用
        Files.walkFileTree(Paths.get("./wxk1991"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("====>进入目录:" + dir);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("====>遍历到文件" + file.toFile().getName());
                System.out.println("执行删除");
                file.toFile().delete();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 遍历完目录该干的事，我们进入目录时存在文件，删不了，但是我们遍历完文件并删除后，等退出目录时，已经空了，可以删除
                System.out.println("<====退出目录" + dir);
                System.out.println("执行删除");
                dir.toFile().delete();
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    @Test
    public void CopyMultiplyDirectory() throws IOException {
        // 拷贝多级目录
        String src = "xxx1";
        String dst = "xxx2";

        // lambda表达式内部的异常是不会被抛出的 ，这应该和线程设计有关系，因为Runnable接口就是不向外抛出的，需要自己处理
        // ?? 个人认为。lambda表达式主要是给线程创建提供的语法糖，这样才导致了其他地方的lambda表达式也有了线程对象的一些特点
        // 比如需要手动处理异常，或者无法引用外部变量，需要使用特定的atomic类
        Files.walk(Paths.get(src)).forEach((path)->{
            String targetName = path.toString().replace(src,dst);
            if (Files.isDirectory(path)) {
                // 是目录则创建
                try {
                    Files.createDirectory(Paths.get(targetName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (Files.isRegularFile(path)) {
                // 是文件则拷贝
                try {
                    Files.copy(path,Paths.get(targetName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
