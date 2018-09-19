package com.tjfintech.common;


import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StoreTest {

    private  final static int   SLEEPTIME=5*1000;
    Store store=new Store();
//    int number =1;
//        log.info("\n创建存证交易--------------------------------\n");
//    String response=CreateStore();
//    JSONObject jsonObject=JSONObject.fromObject(response);
//    String hash=jsonObject.getJSONObject("Data").get("Figure").toString();
//        log.info("\n创建带密码存证交易--------------------------------\n");
//    String responsePwd=CreateStorePwd();
//    JSONObject jsonObjectPwd=JSONObject.fromObject(responsePwd);
//    String hashPwd=jsonObjectPwd.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(SLEEPTIME);//休眠5秒
//        log.info("\n查询存证交易--------------------------------\n");
//    GetStore(hash);
//        log.info("\n获取隐私存证--------------------------------\n");
//    GetStorePost(hash);
//        log.info("\n获取带密码隐私存证--------------------------------\n");
//    GetStorePostPwd(hashPwd);
//        log.info("\n获取交易索引--------------------------------\n");
//    GetTransactionIndex(hash);
//        log.info("\n获取区块高度--------------------------------\n");
//    GetHeight();
//    // GetBlockByHash();
//        log.info("\n按高度获取区块信息--------------------------------\n");
//    GetBlockByHeight(number);
//        log.info("\n交易复杂2查询--------------------------------\n");
//    GetTxSearch(2);
//        log.info("\n交易复杂1查询--------------------------------\n");
//    GetTxSearch(1);
//        log.info("\n查询交易是否存在于钱包数据库");
//    GetInlocal(hash);
//        log.info("\n统计某种交易类型的数量");
//    GetStat();
    @Test
    public void getTransaction() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response=store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransaction(storeHash);
        assertThat(response2,containsString("200"));
    }


    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_createStore() throws Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response=store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));

    }

    /**
     *TC10-创建隐私存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHashPwd中用于查询测试
     */

    @Test
    public void TC09_createStorePwd()throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1=store.CreateStorePwd(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1,containsString("Data"));


    }
    /**
     *TC06-获取存证交易byhash
     * 通过TC05全局变量storeHash用于查询测试
     * 预期：返回200，Data为存证内容
     */
    @Test
    public void getStore() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(3)+"\"";
        String response=store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String  storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2=store.GetStore(storeHash);
        assertThat(response2, containsString("200"));
        assertThat(response2,containsString("json"));
    }
    /**
     *TC09-获取隐私存证交易byhash
     * 通过TC05全局变量storeHash用于查询测试
     * 预期：返回200，Data为存证内容
     * 错误私钥3，返回存证内容不正确
     */
    @Test
    public void getStorePost() throws Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1=store.CreateStorePwd(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2=store.GetStorePost(StoreHashPwd,PRIKEY1);
        String response3=store.GetStorePostPwd(StoreHashPwd,PRIKEY6,PWD6);
        String response4=store.GetStorePost(StoreHashPwd,PRIKEY3);
        assertThat(response2, containsString("200"));
        assertThat(response2,containsString("json"));
        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("json"));
        assertThat(response4, containsString("200"));
        assertEquals(response4.contains("json"), false);
    }










    @Test
    public void getTransactionIndex() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response=store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
       String response2=store.GetTransactionIndex(storeHash);
       assertThat(response2,containsString("200"));
       assertThat(response2,containsString("success"));
    }

    @Test
    public void getHeight() {
        String response=store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
       Integer  height=jsonObject.getInt("Data");
        assertThat(response,containsString("200"));


    }

    @Test
    public void getBlockByHeight() {
        String response=store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("Data");
        assertThat(response,containsString("200"));
        int Height=4;
        String response2=store.GetBlockByHeight(height-1);
        assertThat(response2,containsString("200"));
    }

    @Test
    public void getBlockByHash() {
    }

    @Test
    public void getTxSearch() {
       String response=store.GetTxSearch(0,5,"tor");
       assertThat(response,containsString("200"));
    }

    @Test
    public void getInlocal()  throws Exception{
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response=store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2=store.GetInlocal(storeHash);
        assertThat(response2,containsString("200"));
    }

    @Test
    public void getStat() {
        String type="type";
      String response=store.GetStat(type);
        assertThat(response,containsString("200"));
    }
}
