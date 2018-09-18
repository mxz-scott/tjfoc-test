package com.tjfintech.common;

import com.tjfintech.common.untils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class StoreTestInvaild {
    public static String SDK_ADD="http://10.1.3.165:9990";
    Store store=new Store();
    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }
    @Test
    public void runSDK3TestInvalid() throws  Exception{
        log.info("创建空值存证交易");
        String responseInvaid=CreateStoreNull();
        JSONObject jsonObjectInvaild=JSONObject.fromObject(responseInvaid);
        String hashInvalid1="错误的hash值";
        String hashInvalid2=jsonObjectInvaild.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(1000*5);//休眠5秒
        log.info("查询错误存证内容");
        store.GetStore(hashInvalid1);
        store.GetStore(hashInvalid2);
        log.info("查询错误隐私存证内容");
        store.GetStorePost(hashInvalid2,"123");
    }
    /**
     * 创建存证交易空值
     * @author chenxu
     * @version 1.0
     * @method  POST
     */
    public   String   CreateStoreNull(){
        String Data="";//存储内容为空
        String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWpFZUc0Vm9ETTJkRjAxWnpGQ3NQNkxqTE9zVC8NCkg2YWx5ejBNRXRSU2krazQxbTNzOXFoUVB4UDk1OFFQdGUwS2pZa1VKeUt0MUVBV2NraEI0Wm16eUE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        List<String> PubkeysObjects=new ArrayList<String>();
        PubkeysObjects.add(Pubkeys);
        Map<String,Object> map=new HashMap<>();
        map.put("Data",Data);
        map.put("Pubkeys",PubkeysObjects);
        String result= PostTest.sendPostToJson(SDK_ADD+"/store", map);
        log.info(result);
        return result;
    }
    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }






}
