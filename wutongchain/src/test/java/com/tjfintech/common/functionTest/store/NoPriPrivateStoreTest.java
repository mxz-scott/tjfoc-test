package com.tjfintech.common.functionTest.store;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
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
public class NoPriPrivateStoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GoPrivateStore priStore = new GoPrivateStore();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    CertTool certTool = new CertTool();

    @BeforeClass
    public static void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        Thread.sleep(2000);
    }

    /**
     *  不带私钥获取隐私存证
     */
    @Test
    public void getPriStoreNoPrivacy() throws Exception {
        String Data = "cxTest-private" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreatePrivateStore(Data,map);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        String StoreHashPwd = jsonObject.get("data").toString();

        commonFunc.sdkCheckTxOrSleep(StoreHashPwd,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //使用不带密码私钥获取隐私存证
        String response2= priStore.GetPrivacyStore(StoreHashPwd,PUBKEY1);
        assertThat(response2, containsString("200"));

        String secretKey = JSONObject.fromObject(JSONObject.fromObject(response2).getString("Data")).getString("SecretKey");
        String secretData = JSONObject.fromObject(JSONObject.fromObject(response2).getString("Data")).getString("SecretData");

        String plainKey = certTool.decryptKey(PEER2IP,secretKey,PRIKEY1,"");
        assertEquals(Data,certTool.decryptPriData(PEER2IP,plainKey,secretData));


        //使用带密码私钥获取隐私存证
        response2= priStore.GetPrivacyStore(StoreHashPwd,PUBKEY6);
        assertThat(response2, containsString("200"));

        secretKey = JSONObject.fromObject(JSONObject.fromObject(response2).getString("data")).getString("SecretKey");
        secretData = JSONObject.fromObject(JSONObject.fromObject(response2).getString("data")).getString("SecretData");
        plainKey = certTool.decryptKey(PEER2IP,secretKey,PRIKEY6,PWD6);
        assertEquals(Data,certTool.decryptPriData(PEER2IP,plainKey,secretData));

        String response4= store.GetStorePost(StoreHashPwd,PRIKEY3);
        assertThat(response4, containsString("500"));
        assertThat(response4, containsString("you have no permission to get this transaction !"));
    }



    @Test
    public void NoPrivacyAuthorizeAndGet()throws  Exception{

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
        String hash = jsonObject.get("data").toString();
        String res3 = priStore.GetPrivacyStore(hash,PUBKEY7);
        assertThat(res3,containsString("200"));


        String secretKey = JSONObject.fromObject(JSONObject.fromObject(res3).getString("Data")).getString("SecretKey");
        String secretData = JSONObject.fromObject(JSONObject.fromObject(res3).getString("Data")).getString("SecretData");
        String plainKey = certTool.decryptKey(PEER2IP,secretKey,PRIKEY7,PWD7);
        assertEquals(Data,certTool.decryptPriData(PEER2IP,plainKey,secretData));


        //使用PRIKEY7对PUBKEY2授权访问交易哈希为hash
        AuthorizeTest(PEER2IP,hash,plainKey,
                PRIKEY7,PWD7,PUBKEY7,
                PUBKEY2);

        //使用新被授权的公钥或者隐私存证并解密
        String res5 = priStore.GetPrivacyStore(hash,PUBKEY2);
        secretKey = JSONObject.fromObject(JSONObject.fromObject(res5).getString("Data")).getString("SecretKey");
        secretData = JSONObject.fromObject(JSONObject.fromObject(res5).getString("Data")).getString("SecretData");
        plainKey = certTool.decryptKey(PEER2IP,secretKey,PRIKEY2,"");
        assertEquals(Data,certTool.decryptPriData(PEER2IP,plainKey,secretData));

        //使用PRIKEY7对PUBKEY2授权访问交易哈希为hash
        AuthorizeTest(PEER2IP,hash,plainKey,
                PRIKEY7,PWD7,PUBKEY7,
                PUBKEY6);

        //使用新被授权的公钥或者隐私存证并解密
        String res6 = priStore.GetPrivacyStore(hash,PUBKEY6);
        secretKey = JSONObject.fromObject(JSONObject.fromObject(res6).getString("Data")).getString("SecretKey");
        secretData = JSONObject.fromObject(JSONObject.fromObject(res6).getString("Data")).getString("SecretData");
        plainKey = certTool.decryptKey(PEER2IP,secretKey,PRIKEY6,PWD6);
        assertEquals(Data,certTool.decryptPriData(PEER2IP,plainKey,secretData));

    }


    public void AuthorizeTest(String peerIP,
                              String txHash,String plainKey,
                              String ownerPri,String ownPwd,String ownerPub,
                              String toPub )throws Exception{

        //以下为给PUBKEY赋权限操作步骤 需要对请求中的字段单独获取
        List<Map> listSecrets = new ArrayList<>();

        Map<String,Object>mapSecrets = new HashMap<>();
        mapSecrets.put("PubKey",toPub);
        mapSecrets.put("SecurityKey",certTool.encryptKeyWithPub(peerIP,plainKey,toPub));

        listSecrets.add(mapSecrets);
        String listStr = JSON.toJSON(listSecrets).toString();
        listStr = listStr.replaceAll("\"","\\\\\"");

        String Sign = certTool.sign(peerIP,ownerPri,ownPwd,listStr,"hex");

        String res1 = priStore.PrivacyStoreAuthorize(txHash, listSecrets,ownerPub,Sign);  //给toPub赋权限
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


    }



}
