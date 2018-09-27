package com.tjfintech.common;


import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StoreTest {

    public   final static int   SLEEPTIME=5*1000;
    Store store=new Store();




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
     *TC06-获取存证交易byhash
     * 通过TC05全局变量storeHash用于查询测试
     * 预期：返回200，Data为存证内容
     */
    @Test
    public void TC06_getStore() throws  Exception {
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
     * 预期：返回200，Data为存证内容
     * 错误私钥3，返回存证内容不正确
     */
    @Test
    public void getStorePost() throws Exception {
        String Data = "cxTest-" + UtilsClass.Random(4);
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

        assertThat(response2, containsString(Data));
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString(Data));
        assertThat(response4, containsString("200"));
        assertEquals(response4.contains(Data), false);
    }

    /**
     * TC07复杂查询存证交易，数据格式为String
     * 创建2笔存证交易，数据格式为string
     * 使用复杂查询接口查询交易，size设为1
     * 使用复杂查询接口查询交易，size设为5
     * 使用步骤1的交易哈希查询tx/search
     */
    @Test
    public void TC07_GetTxSearch() throws Exception {
        String Data = "cxtest-" + UtilsClass.Random(1);
        String Data2 = "cxtest-" + UtilsClass.Random(2);
        String response = store.CreateStore(Data);
        String response2 = store.CreateStore(Data2);
        JSONObject jsonObject = JSONObject.fromObject(response);
        JSONObject jsonObject2 = JSONObject.fromObject(response2);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String storeHash2 = jsonObject2.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response3 = store.GetTxSearch(0, 1, "cxtest");
        String response4 = store.GetTxSearch(0, 5, "cxtest");
        JSONObject jsonObject3 = JSONObject.fromObject(response3);
        JSONObject jsonObject4 = JSONObject.fromObject(response4);
        assertEquals(jsonObject3.getJSONArray("Data").size() == 1, true);
        assertEquals(jsonObject4.getJSONArray("Data").size() == 5, true);
        String response5 = store.GetStore(storeHash);
        String response6 = store.GetStore(storeHash2);
        assertThat(response5, containsString("200"));
        assertThat(response6, containsString("200"));

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
        map.put("pubkeys",PUBKEY6);
        String response1=store.CreateStorePwd(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1,containsString("Data"));


    }

    /**
     * TC11复杂查询隐私存证交易，数据格式为String
     * 创建2笔隐私存证交易，数据格式为string
     * 使用复杂查询接口查询交易，size设为1
     * 使用复杂查询接口查询交易，size设为5
     * 使用步骤1的交易哈希查询tx/search
     */
    @Test
    public void TC11_GetTxSearchPwd() throws Exception {
        String data = "Testcx-" + UtilsClass.Random(2);
        String data2 = "Testcx-" + UtilsClass.Random(4);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response = store.CreateStorePwd(data,map);
        String response2 = store.CreateStorePwd(data2,map);
        JSONObject jsonObject = JSONObject.fromObject(response);
        JSONObject jsonObject2 = JSONObject.fromObject(response2);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        String storeHash2 = jsonObject2.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
//        String response3 = store.GetTxSearch(0, 1, "Testcx-");
//        String response4 = store.GetTxSearch(0, 5, "Testcx-");
//        JSONObject jsonObject3 = JSONObject.fromObject(response3);
//        JSONObject jsonObject4 = JSONObject.fromObject(response4);
//        assertEquals(jsonObject3.getJSONArray("Data").size() == 1, true);
//        assertEquals(jsonObject4.getJSONArray("Data").size() == 5, true);
        String response5 = store.GetStorePostPwd(storeHash,PRIKEY6,PWD6);
        String response6 = store.GetStorePost(storeHash2,PRIKEY1);
        assertThat(response5, containsString("200"));
        assertThat(response5,containsString(data));
        assertThat(response6, containsString("200"));
        assertThat(response6,containsString(data2));

    }

    /**
     * TC15 统计交易数量
     * 查询后发起交易再查询总数是否加一
     * 预期num2==num+1
     * @throws Exception
     */
    @Test
    public void TC15_getStat() throws Exception {
        String type = "1";
        String response=store.GetStat(type);
        int num = JSONObject.fromObject(response).getJSONObject("Data").getInt("Total");
        String Data = "\"test\":\"json" + UtilsClass.Random(4) + "\"";
        String response2 = store.CreateStore(Data);
        Thread.sleep(SLEEPTIME);
        String response3 = store.GetStat(type);
        int num2 = JSONObject.fromObject(response3).getJSONObject("Data").getInt("Total");
        assertEquals(num2 == (num + 1), true);
        assertThat(response,containsString("200"));
    }

    /**
     * TC18 同时发送10笔存证交易
     * @throws Exception
     */
   @Test
   public void TC18_CreateStore10()throws  Exception{
       List<String>list=new ArrayList<>();
       for(int i=0;i<10;i++){
           list.add(store.CreateStore("cx"+UtilsClass.Random(4)));
       }
       for(int i=0;i<list.size();i++){
           assertThat(list.get(i), containsString("200"));
           JSONObject jsonObject=JSONObject.fromObject(list.get(i));
           String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
          assertEquals(storeHash.isEmpty(),false);
       }

       Thread.sleep(SLEEPTIME);


   }


  //TODO
//----------------------------------------------------------------------------------------------------------------


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
//TODO  根据哈希查询区块为空
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


}
