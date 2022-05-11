package cn.wjb114514.c1;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @since 1.7
 * java的相对路径以 System.getProperty("user.dir")指定
 * 一般都是项目根路径
 *
 */
public class PathAndPaths {
    public static void main(String[] args) {
        // C:\Users\DELL\IdeaProjects\nettyStudy
        System.out.println(System.getProperty("user.dir"));

        // Path:接口类 Paths：接口的工具类
        Path path = Paths.get("a.txt");// 不指定盘符就是相对路径，或者linux中不指定/ 就是相对路径
        System.out.println(path);

        // File类 可以创建文件，目录。不可以创建多级目录
        // Files.copy：直接调用底层操作系统实现文件拷贝，效率较高。可以指定参数进行覆盖写。

        // Files.move() Files.delete() ...
    }
}
