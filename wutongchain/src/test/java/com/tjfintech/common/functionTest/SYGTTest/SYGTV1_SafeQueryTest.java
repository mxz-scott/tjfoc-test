package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SYGTV1_SafeQueryTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();
    SYGTCommonFunc sygtCF = new SYGTCommonFunc();


    @Before
    public void updateSceneLables()throws Exception{
        List<Map> listScenes = new ArrayList<>();
        Map mapScene = new HashMap();
        mapScene.put("code","1");mapScene.put("name","反洗钱名单");      listScenes.add(mapScene);
        mapScene.put("code","2");mapScene.put("name","恶意投诉客户名单");listScenes.add(mapScene);
        mapScene.put("code","3");mapScene.put("name","疑似倒买倒卖名单");listScenes.add(mapScene);

        List<Map> listLabels = new ArrayList<>();
        Map mapLabel = new HashMap();
        mapLabel.put("code","4");mapLabel.put("name","高风险名单");listLabels.add(mapLabel);
        mapLabel.put("code","5");mapLabel.put("name","低风险名单");listLabels.add(mapLabel);

        sygt.SSSettingUpdate(listScenes,listLabels);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
    }

    /**
     * 单笔匿踪查询上链
     * @throws Exception
     */
    @Test
    public void onSingleSafeQueryOnChainTest() throws Exception {
        String requestID = "12345645" + Random(26);
        String partyA = account1;
        String partyB = account3;
        String replyDigest = "digest" + Random(12);
        String createdTime = "2021-03-30 12:00:00";
        String respTime = "2021-03-30 12:00:01";

        String response = "";

        response = sygt.SSSingleSafeQueryRequest(requestID,partyA,partyB,replyDigest,createdTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情


        response = sygt.SSSingleSafeQueryReply(requestID,respTime,replyDigest);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

        response = sygt.SSSingleSafeQueryComplete(requestID,true,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

        response = sygt.SSSingleSafeQueryComplete(requestID,false,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

    }


    /**
     * 批量匿踪查询上链
     * @throws Exception
     */
    @Test
    public void onMultiSafeQueryOnChainTest() throws Exception {
        String requestID = "12345645" + Random(26);
        String partyA = account1;
        String partyB = account3;
        String replyDigest = "digest" + Random(12);
        String createdTime = "2021-03-30 12:00:00";
        String respTime = "2021-03-30 12:00:01";

        String response = "";

        response = sygt.SSMultiSafeQueryRequest(requestID,partyA,partyB,replyDigest,createdTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情


        response = sygt.SSMultiSafeQueryReply(requestID,respTime,replyDigest);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

        response = sygt.SSMultiSafeQueryComplete(requestID,true,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

        response = sygt.SSMultiSafeQueryComplete(requestID,false,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        //检查交易详情

    }

    /***
     * 盟主1进行查询
     * 当前不确定实现 先占位
     * @throws Exception
     */
    @Test
    public void safeQueryDo_A()throws Exception{
        safeQueryDo(SDKURL1);
    }

    /***
     * 盟主2进行查询
     * 当前不确定实现 先占位
     * @throws Exception
     */
    @Test
    public void safeQueryDo_B()throws Exception{
        safeQueryDo(SDKURL1);
    }

    /***
     * 成员进行查询
     * 当前不确定实现 先占位
     * @throws Exception
     */
    @Test
    public void safeQueryDo_C()throws Exception{
        safeQueryDo(SDKURLm1);
    }

    public void safeQueryDo(String sdkurl)throws Exception{
        SDKADD = sdkurl;
        String response = "";
        String scene = "";
        String label = "";
        Map inputs = new HashMap();

        log.info("三方ABC数据查询");
        inputs.put("id","ABC三方0001");
        inputs.put("id","ABC三方0002");
        inputs.put("id","ABC三方0003");
        inputs.put("id","ABC三方0004");
        inputs.put("id","ABC三方0005");
        inputs.put("id","ABC三方0006");
        inputs.put("id","ABC三方0007");
        inputs.put("id","ABC三方0008");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方AB数据查询");
        inputs.clear();
        inputs.put("id","AB两方00001");
        inputs.put("id","AB两方00002");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方AC数据查询");
        inputs.clear();
        inputs.put("id","AC两方00002");
        inputs.put("id","AC两方00003");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方BC数据查询");
        inputs.clear();
        inputs.put("id","BC两方00001");
        inputs.put("id","BC两方00002");
        inputs.put("id","BC两方00003");
        inputs.put("id","BC两方00004");
        inputs.put("id","BC两方00005");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("单方A数据查询");
        inputs.clear();
        inputs.put("id","A方000012");
        inputs.put("id","A方000013");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("单方B数据查询");
        inputs.clear();
        inputs.put("id","B方000003");
        inputs.put("id","B方000004");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("单方C数据查询");
        inputs.clear();
        inputs.put("id","C方000001");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("单方*3数据查询");
        inputs.clear();
        inputs.put("id","A方000012");
        inputs.put("id","A方000013");
        inputs.put("id","B方000003");
        inputs.put("id","B方000004");
        inputs.put("id","C方000001");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        log.info("多类型复杂数据查询");
        inputs.clear();
        inputs.put("id","ABC三方0009");
        inputs.put("id","ABC三方0010");
        inputs.put("id","AB两方00003");
        inputs.put("id","AC两方00001");
        inputs.put("id","BC两方00002");
        inputs.put("id","A方000012");
        inputs.put("id","A方000013");
        inputs.put("id","B方000003");
        inputs.put("id","B方000004");
        inputs.put("id","C方000001");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
    }


}
