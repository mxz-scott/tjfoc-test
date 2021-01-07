package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PrivateStoreTestFastGet {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    @BeforeClass
    public static void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        Thread.sleep(2000);
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
        String StoreHashPwd = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleepNoDBQuery(StoreHashPwd,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String response2= store.GetStorePost(StoreHashPwd,PRIKEY1);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(Data));

        String response3= store.GetStorePostPwd(StoreHashPwd,PRIKEY6,PWD6);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString(Data));

        String response4= store.GetStorePost(StoreHashPwd,PRIKEY3);
        int state = JSONObject.fromObject(response4).getInt("state");
        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }
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
        String StoreHashPwd = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleepNoDBQuery(StoreHashPwd,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(response1, containsString("200"));
        assertThat(response1,containsString("data"));
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
        String storeHash = jsonObject.getString("data");
        String storeHash2 = jsonObject2.getString("data");

        commonFunc.sdkCheckTxOrSleepNoDBQuery(storeHash2,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

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

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1,"");
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);


        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY1,"");
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

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

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);


        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

    }

    @Test
    public void TC1811_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY7);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY7,PWD7);
        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String res5 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        String res6 = store.GetStorePost(hash,PRIKEY5);
        int state = JSONObject.fromObject(res6).getInt("state");
        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }
        assertThat(res6,containsString("you have no permission to get this transaction !"));

        String res7 = store.GetStorePostPwd(hash,PRIKEY6,PWD7);
        assertThat(res7,containsString("400"));
        assertThat(res7,containsString("Invalid private key or the password is not match the private key"));

        String res8 = store.GetStorePostPwd(hash,PRIKEY1,PWD6);
        assertThat(res8,containsString("400"));
        assertThat(res8,containsString("Invalid private key or the password is not match the private key"));


        res8 = store.GetStorePost(hash,"123456");
        assertThat(res8,containsString("400"));
        assertThat(res8,containsString("illegal base64 data at input byte"));

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

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));
        res3 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));
        res3 = store.GetStorePost(hash,PRIKEY3);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY4);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1,"");
        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));
        String res4 = store.GetStorePost(hash,PRIKEY4);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY5);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY2,""); //只有第一个私钥可以授权

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner"));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        res2 = store.StoreAuthorize(hash, map, PRIKEY3,"");

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner")); //只有第一个私钥可以授权

    }

    @Test
    public void TC1813_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKey1",PUBKEY6);
        map.put("pubKey2",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));
        res3 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res3,containsString("200"));
        jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY7);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY6, PWD6);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePostPwd(hash,PRIKEY7, PWD7);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY4);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1,"");

        assertThat(res1,containsString("500"));
        assertThat(res1,containsString("private key is not tx owner"));

    }


    @Test
    public void TC1814_CreatePrivateStore()throws  Exception{

        String Data = "test" + UtilsClass.Random(4);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String res2 = store.StoreAuthorize(hash, map, PRIKEY6, PWD6);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res2,containsString("200"));
        assertThat(res2,containsString("success"));
        String res5 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res5,containsString("200"));
        jsonResult=JSONObject.fromObject(res5);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);
        res2 = store.StoreAuthorize(hash, map, PRIKEY1,"");

        assertThat(res2,containsString("500"));
        assertThat(res2,containsString("private key is not tx owner"));

    }

    @Test
    public void TC2166_CreatePrivateStore()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys1",PUBKEY1);
        map.put("pubKeys2",PUBKEY2);
        map.put("pubKeys3",PUBKEY7);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY6,PWD6);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));

        String res4 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        res4 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

    }

    @Test
    public void TC2167_CreatePrivateStore()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test3", true);
        result.put("test4", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        map=new HashMap<>();
        map.put("pubKeys1",PUBKEY3);
        map.put("pubKeys2",PUBKEY6);
        map.put("pubKeys3",PUBKEY7);
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1,"");

        commonFunc.sdkCheckTxOrSleepNoDBQuery(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));

        String res4 = store.GetStorePost(hash,PRIKEY3);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        res4 = store.GetStorePostPwd(hash,PRIKEY6,PWD6);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

        res4 = store.GetStorePostPwd(hash,PRIKEY7,PWD7);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));

    }
}
