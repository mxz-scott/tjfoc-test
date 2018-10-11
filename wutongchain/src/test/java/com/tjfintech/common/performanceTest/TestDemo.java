package com.tjfintech.common.performanceTest;

import com.tjfintech.common.GoStore;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class TestDemo {
     static TestBuilder testBuilder= TestBuilder.getInstance();
    static   Store store =testBuilder.getStore();


    public static void main(String[] args) throws InterruptedException {
        TestDemo testDemo = new TestDemo();

        long timeTasks = testDemo.timeTasks(10, new Runnable() {
            @Override
            public void run() {
                try {//每个线程执行的内容
                    Thread.sleep(1000);
                    String data = store.CreateStore("cx"+ UtilsClass.Random(5));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("测试所用时间:{}毫秒", timeTasks);
    }

    public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
        //预热，编译
        for (int i = 0; i < 10; i++) {
            task.run();
        }
        // 真正的测试
        // 使用同步工具类，保证多个线程同时（近似同时）执行
        final CountDownLatch startGate = new CountDownLatch(1);
        // 使用同步工具类，用于等待所有线程都运行结束时，再统计耗时
        final CountDownLatch endGate = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        startGate.await();
                        try {

                            task.run();

                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        long start = System.currentTimeMillis();
        startGate.countDown();
        endGate.await();
        long end = System.currentTimeMillis();
        return end - start;
    }
}


