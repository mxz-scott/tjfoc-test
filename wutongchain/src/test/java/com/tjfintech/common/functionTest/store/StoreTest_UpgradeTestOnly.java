package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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
public class StoreTest_UpgradeTestOnly {

    public   final static int   SHORTSLEEPTIME=3*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    @BeforeClass
    public static void BeforeTest()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
    }

    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_createStore() throws Exception {

        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");
        assertEquals(globalResponse,response);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));

    }

    /**
     *tc277获取隐私存证交易byhash
     * 预期：返回200，Data为存证内容
     * 错误私钥3，返回存证内容不正确
     */
    @Test
    public void TC277_getStorePost() throws Exception {
        String Data = "cxTest-" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

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


    @Test
    public void TC278_createBigSizeStore() throws Exception {

        String Data = UtilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String Data2 = UtilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = store.CreateStore(Data2);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String Data3 = UtilsClass.Random(10) + utilsClass.readStringFromFile(resourcePath
                + "bigsize3.txt");
        String response3 = store.CreateStore(Data3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    /**
     *TC09-创建隐私存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     *
     */

    @Test
    public void TC09_createStorePwd()throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String response1= store.CreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1,containsString("data"));

    }

    @Test
    public void TC279_getTxDetail() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String response2= store.GetTxDetail(storeHash);
        assertThat(response2,containsString("200"));

        String args=JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("txId");

        assertEquals(args.equals(storeHash),true);

    }

    /**
     * tc279获取交易索引
     * @throws Exception
     */
    @Test
    public void TC279_getTransactionIndex() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
       String response2= store.GetTransactionIndex(storeHash);
       assertThat(response2,containsString("200"));
       assertThat(response2,containsString("success"));
    }

    /**
     * Tc275获取区块高度
     */
    @Test
    public void TC275_getHeight() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));


    }

    /**
     * TC274根据高度查询某个区块信息
     */
    @Test
    public void TC274_getBlockByHeight() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        int Height=4;
        String response2= store.GetBlockByHeight(Height-1);
        assertThat(response2,containsString("200"));
    }

    @Test
    public void TC243_getBlockByHash() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("data");
        assertThat(response,containsString("200"));
        String response2= store.GetBlockByHeight(height-2);
        String hash=JSONObject.fromObject(response2).getJSONObject("data").getJSONObject("header").getString("blockId");
        String response3= store.GetBlockByHash(hash);
        assertEquals(response2.equals(response3),true);
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
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY1);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

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
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

    }


}
