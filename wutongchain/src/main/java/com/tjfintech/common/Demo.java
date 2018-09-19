package com.tjfintech.common;

import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.MultiSign.SDKADD;
@Slf4j
public class Demo {
    /**
     * 类测试开始时，会执行一次的方法
     * 前置条件
     */
    @BeforeClass
    public static void config(){}

    /**
     * 每个方法测试开始时，都会执行一次的方法
     * 前置条件
     */
    @Before
    public  void beforeConfig(){}

    /**
     * post请求demo
     *{"Pubkeys":["demoPubkeys"],"Data":"demo","M":"demoM"}
     */
    @Test
    public void demoPost(){
        String Data = "demo";
        String M="demoM";
        String Pubkeys="demoPubkeys";
        List<String> PubkeysObjects=new ArrayList<String>();
        PubkeysObjects.add(Pubkeys);
        Map<String,Object> map=new HashMap<>();
        map.put("Data",Data);
        map.put("M",M);
        map.put("Pubkeys",PubkeysObjects);
        log.info(PostTest.sendPostToJson(SDKADD+"/demo", map));
       /* String response= PostTest.sendPostToJson(SDKADD+"/demo",map);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();*/
    }

    /**
     * get请求demo
     * SDKADD/demo?Data=demo&Pubkeys=demoPubkeys
     */
    @Test
    public void demoGet(){
        String Data="demo";
        String PubKeys="demoPubkeys";
        Map<String,Object>map=new HashMap<>();
        map.put("Data",Data);
        map.put("Pubkeys",PubKeys);
        String param= GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/demo"+"?"+param));
    }

    /**
     * 每个方法测试结束后都会执行的方法
     * 后置处理
     */
    @After
    public  void revert(){}

    /**
     * 类测试结束后会执行的方法
     * 后置处理
     */
    @AfterClass
    public static void aferRevert(){}
}
