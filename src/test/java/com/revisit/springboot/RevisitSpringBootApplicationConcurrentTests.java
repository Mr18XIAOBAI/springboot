/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: RevisitSpringBootApplicationConcurrentTests
 * Author:   Revisit-Moon
 * Date:     2019-06-06 16:05
 * Description: RevisitSpringBootApplicationConcurrentTests
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019-06-06 16:05        1.0              描述
 */

package com.revisit.springboot;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.utils.MoonUtil;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 〈RevisitSpringBootApplicationConcurrentTests〉
 *
 * @author Revisit-Moon
 * @create 2019-06-06
 * @since 1.0.0
 */
// @RunWith(SpringRunner.class)
@SpringBootTest
public class RevisitSpringBootApplicationConcurrentTests {

    @Test
    public void contextLoads() {

    }

    public static void main(String[] args) {
        //定义并发次数
        int row = 1000;
        //初始化
        JSONObject requestData = new JSONObject();
        requestData.put("userName", "MeiGuoTec");
        requestData.put("password", MoonUtil.encodeByMD5("888888"));

        Thread thread = new Thread(){
            @Override
            public void run() {
                System.out.println("所有并发任务执行完毕");
            }
        };
        System.out.println("开始并发测试");
        CyclicBarrier cyclicBarrier = new CyclicBarrier(row, thread);
        initTask(cyclicBarrier,row,requestData);
    }

    public static void initTask(CyclicBarrier cyclicBarrier, int row, JSONObject requestData) {
        for (int i = 0; i < row; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    taskInfo(requestData);
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
    }

    private static void taskInfo(JSONObject requestData) {
        JSONObject resultBean = MoonUtil.httpClientPostStr("http://localhost:8080/api/user/login", requestData.toJSONString());
        if (resultBean == null || resultBean.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + " 任务执行失败,当前时间: " + MoonUtil.getNowTimeToyMdHms());
        } else {
            System.out.println(resultBean.toJSONString());
        }
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class MyThread implements Runnable{
        @Override
        public void run() {
            JSONObject requestData = new JSONObject();
            requestData.put("userName", "MeiGuoTec");
            requestData.put("password", MoonUtil.encodeByMD5("888888"));
            JSONObject resultBean = MoonUtil.httpClientPostStr("http://localhost:8080/api/user/login", requestData.toJSONString());
            if (resultBean == null || resultBean.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + " 任务执行失败,当前时间: " + MoonUtil.getNowTimeToyMdHms());
            } else {
                System.out.println(resultBean.toJSONString());
            }
        }
    }
}
