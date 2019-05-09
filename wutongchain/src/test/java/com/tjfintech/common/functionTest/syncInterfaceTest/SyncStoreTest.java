package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY3;
import static com.tjfintech.common.utils.UtilsClass.PWD6;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncStoreTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    Store store =testBuilder.getStore();
    public   final static int   SLEEPTIME=8*1000;
    public   final static int   SHORTSLEEPTIME=3*1000;
    /**
     * TC05-同步创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_SynCreateStore() throws Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.SynCreateStore(utilsClass.LONGTIMEOUT,Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
    }

    /**
     * TC05-同步创建存证交易，数据格式为Json  --带公钥
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_SynCreateStoreCarryPub() throws Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.SynCreateStore(utilsClass.LONGTIMEOUT,Data,PUBKEY1,PUBKEY2);
        JSONObject jsonObject=JSONObject.fromObject(response);
        jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
    }



    /**
     *tc277同步创建隐私存证交易byhash
     * 获取隐私存证交易byhash --带密码
     */
    @Test
    public void TC277_SynCreateStorePwd() throws Exception {
        String Data = "cxTest-" + UtilsClass.Random(7);
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.SynCreateStorePwd(utilsClass.LONGTIMEOUT,Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2 = store.SynGetStorePost(utilsClass.LONGTIMEOUT, StoreHashPwd, PRIKEY6, PWD6); //获取隐私存证
        System.out.println(response2);
    }




}
