package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PrivateStoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    @BeforeClass
    public static void beforeConfig() throws Exception {
        if (certPath != "" && bReg == false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            Thread.sleep(SLEEPTIME);
            bReg = true;
        }
    }

    /**
     *tc277获取隐私存证交易byhash
     * 预期：返回200，Data为存证内容
     * 错误私钥3，返回存证内容不正确
     */
    @Test
    public void TC277_getStorePost() throws Exception {
        String Data = "cxTest-private" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);

        String response2= store.GetStorePost(StoreHashPwd,PRIKEY1);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(Data));

        String response3= store.GetStorePostPwd(StoreHashPwd,PRIKEY6,PWD6);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString(Data));

        String response4= store.GetStorePost(StoreHashPwd,PRIKEY3);
        assertThat(response4, containsString("500"));
        assertThat(response4, containsString("you have no permission to get this transaction !"));
    }

     /**
     *TC09-创建隐私存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     *
     */

    @Test
    public void TC09_createStorePwd()throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1,containsString("Data"));


    }

    /**
     * TC11查询隐私存证交易，数据格式为String
     * 创建2笔隐私存证交易，数据格式为string
     */
    @Test
    public void TC11_GetStorePwd() throws Exception {
        String data = "Testcx-" + UtilsClass.Random(2);
        String data2 = "Testcx-" + UtilsClass.Random(4);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response = store.CreatePrivateStore(data,map);
        String response2 = store.CreatePrivateStore(data2,map);
        JSONObject jsonObject = JSONObject.fromObject(response);
        JSONObject jsonObject2 = JSONObject.fromObject(response2);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String storeHash2 = jsonObject2.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response5 = store.GetStorePostPwd(storeHash,PRIKEY6,PWD6);
        String response6 = store.GetStorePost(storeHash2,PRIKEY1);
        assertThat(response5, containsString("200"));
        assertThat(response5,containsString(data));
        assertThat(response6, containsString("200"));
        assertThat(response6,containsString(data2));

    }

    // new private store ...
    @Test
    public void TC1803_CreatePrivateStore()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

    }

    @Test
    public void TC1804_CreatePrivateStore()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "50");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY7);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        Thread.sleep(SLEEPTIME);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

    }

    @Test
    public void TC1811_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY7);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));


        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        String res6 = store.GetStorePost(hash,PRIKEY5);
        assertThat(res6,containsString("500"));
        assertThat(res6,containsString("you have no permission to get this transaction !"));

        String res7 = store.GetStorePostPwd(hash,PRIKEY6,PWD7);
        assertThat(res7,containsString("400"));
        assertThat(res7,containsString("Invalid private key or the password is not match the private key"));

        String res8 = store.GetStorePostPwd(hash,PRIKEY1,PWD6);
        assertThat(res8,containsString("400"));
        assertThat(res8,containsString("Invalid private key or the password is not match the private key"));


        res8 = store.GetStorePost(hash,"123456");
        assertThat(res8,containsString("400"));
        assertThat(res8,containsString("illegal base64 data at input byte 4"));

        res8 = store.GetStorePost(hash,"MTIzNDU2");
        assertThat(res8,containsString("400"));
        assertThat(res8,containsString("Invalid private key or the password is not match the private key"));
    }

    @Test
    public void TC1812_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKey1",PUBKEY1);
        map.put("pubKey2",PUBKEY2);
        map.put("pubKey3",PUBKEY3);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));
        res3 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));
        res3 = store.GetStorePost(hash,PRIKEY3);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY4);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY4);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY5);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY2);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner"));
//        String res5 = store.GetStorePost(hash,PRIKEY5);
//        assertThat(res5,containsString("200"));
//        jsonResult=JSONObject.fromObject(res5);
//        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        res2 = store.StoreAuthorize(hash, map, PRIKEY3);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner"));
//        res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
//        assertThat(res5,containsString("200"));
//        jsonResult=JSONObject.fromObject(res5);
//        assertThat(jsonResult.get("Data").toString(),containsString(Data));

    }

    @Test
    public void TC1813_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKey1",PUBKEY6);
        map.put("pubKey2",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));
        res3 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY7);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY6, PWD6);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY7, PWD7);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY4);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        assertThat(res1,containsString("500"));
        assertThat(res1,containsString("private key is not tx owner"));
//        String res4 = store.GetStorePost(hash,PRIKEY4);
//        assertThat(res4,containsString("200"));
//        jsonResult=JSONObject.fromObject(res4);
//        assertThat(jsonResult.get("Data").toString(),containsString(Data));

    }


    @Test
    public void TC1814_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String response= store.CreatePrivateStore(Data,map);
        Thread.sleep(SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String res3 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY6, PWD6);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("Data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        res2 = store.StoreAuthorize(hash, map, PRIKEY1);
        Thread.sleep(SLEEPTIME);
        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner"));

    }


}
