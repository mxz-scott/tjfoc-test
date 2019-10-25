package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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


    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_createStore() throws Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));

    }

    @Test
    public void TC292_getStore() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(3)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String  storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2= store.GetStore(storeHash);
        assertThat(response2, containsString("200"));
        assertThat(response2,containsString("json"));
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


    @Test
    public void TC278_createBigSizeStore() throws Exception {

        String Data = UtilsClass.Random(10) + readStringFromFile(resourcePath +
                "bigsize1.txt");
        String response = store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("Data"));


        String Data2 = UtilsClass.Random(10) + readStringFromFile(resourcePath
                +  "bigsize2.txt");
        String response2 = store.CreateStore(Data2);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("Data"));


        String Data3 = UtilsClass.Random(10) + readStringFromFile(resourcePath
                + "bigsize3.txt");
        String response3 = store.CreateStore(Data3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("Data"));

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
    @Test
    public void TC279_getTxDetail() throws  Exception {
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2= store.GetTxDetail(storeHash);
        assertThat(response2,containsString("200"));
        final Base64.Decoder decoder = Base64.getDecoder();
        String args=JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Header").get("TransactionHash").toString();
        log.info("123{}",args);
        //   .getJSONArray("smartContractArgs").get(0).toString();
        String DataInfo=args;
        // String DataInfo=new String(decoder.decode(args),"UTF-8");
        assertEquals(DataInfo.equals(storeHash),true);

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
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        Thread.sleep(SLEEPTIME);//通过JAVASDK实现的上链要慢，需要再加一个休眠
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
       Integer  height=jsonObject.getInt("Data");
        assertThat(response,containsString("200"));


    }

    /**
     * TC274根据高度查询某个区块信息
     */
    @Test
    public void TC274_getBlockByHeight() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("Data");
        assertThat(response,containsString("200"));
        int Height=4;
        String response2= store.GetBlockByHeight(Height-1);
        assertThat(response2,containsString("200"));
    }
    @Test
    public void TC243_getBlockByHash() {
        String response= store.GetHeight();
        JSONObject jsonObject=JSONObject.fromObject(response);
        Integer  height=jsonObject.getInt("Data");
        assertThat(response,containsString("200"));
        String response2= store.GetBlockByHeight(height-2);
        String hash=JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("header").getString("blockHash");
        String response3= store.GetBlockByHash(hash);
        assertEquals(response2.equals(response3),true);
    }


    /**
     * Tc280交易复杂查询
     */
//    @Test
//    public void TC280_getTxSearch() {
//       String response= store.GetTxSearch(0,5,"tor");
//       assertThat(response,containsString("200"));
//    }



    @Test
    public void TC254_getTransationBlock()throws  Exception{
        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.CreateStore(Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransactionBlock(storeHash);
        assertThat(response2,containsString("200"));
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

    //------------------------------------------------------------------------

    /**
     * 读文件中的字符串
     *
     * @param filepath
     * @return
     */
    public String readStringFromFile(String filepath) {
        String str = "";
        File file = new File(filepath);
        try {
            FileInputStream in = new FileInputStream(file);
            // size 为字串的长度 ，这里一次性读完
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer, "GB2312");
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        }
        return str;
    }
}