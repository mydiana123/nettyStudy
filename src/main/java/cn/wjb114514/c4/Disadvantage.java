package cn.wjb114514.c4;

/**
 * 我们基于 单线程-selector-多channel的模型
 * 1.单线程导致CPU资源无法被充分调度
 * 2.如果某个事件消耗了大量时间，会导致其他操作不受影响。
 * redis底层就使用了此模型，因此redis只适合解决 单次操作时间少的事件
 *
 * 现在的模型：
 * Boss-Worker  Boss负责接客，即建立连接 Worker负责打工，即处理IO事件
 * 其中，建立连接和IO处理的监听任务，都交给自己的selector。
 *
 * 也就是 Boss-Selector ：监听accept事件
 *       Boss-Thread：处理accept事件
 *       Worker-Selector：监听IO事件
 *       Worker-Thread：处理IO事件。
 *
 * 最好让Boss和Worker的数目[因为每个人都有一个线程] 和CPU的核数对应上~
 */
public class Disadvantage {
}
