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


//    @Before
//    public void updateSceneLables()throws Exception{
//        List<Map> listScenes = new ArrayList<>();
//        Map mapScene = new HashMap();
//        mapScene.put("code","1");mapScene.put("name","反洗钱名单");      listScenes.add(mapScene);
//        mapScene.put("code","2");mapScene.put("name","恶意投诉客户名单");listScenes.add(mapScene);
//        mapScene.put("code","3");mapScene.put("name","疑似倒买倒卖名单");listScenes.add(mapScene);
//
//        List<Map> listLabels = new ArrayList<>();
//        Map mapLabel = new HashMap();
//        mapLabel.put("code","4");mapLabel.put("name","高风险名单");listLabels.add(mapLabel);
//        mapLabel.put("code","5");mapLabel.put("name","低风险名单");listLabels.add(mapLabel);
//
//        sygt.SSSettingUpdate(listScenes,listLabels);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//    }

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
        String createdTime = "2021-01-30 12:00:00";
        String respTime = "2021-01-30 12:00:01";

        int hit = 1;

        String response = "";

        response = sygt.SSSingleSafeQueryRequest(requestID,partyA,partyB,createdTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情


        response = sygt.SSSingleSafeQueryReply(requestID,respTime,replyDigest);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情

        response = sygt.SSSingleSafeQueryComplete(requestID,hit,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情

        response = sygt.SSSingleSafeQueryComplete(requestID,hit,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
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
        int hit = 1;

        String response = "";

        response = sygt.SSMultiSafeQueryRequest(requestID,partyA,partyB,createdTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情


        response = sygt.SSMultiSafeQueryReply(requestID,respTime,replyDigest);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情

        response = sygt.SSMultiSafeQueryComplete(requestID,hit,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //检查交易详情

        hit = 0;
        response = sygt.SSMultiSafeQueryComplete(requestID,hit,10000,0,"",respTime);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        txID = commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
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
        //获取初始积分
        int pointA1 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");
        int pointB1 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");
        int pointC1 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");

        SDKADD = sdkurl;
        String response = "";
        String scene = "";
        String label = "";
        Map inputs = new HashMap();

        log.info("ABC数据查询");
        inputs.put("id","ABC0001");
        inputs.put("id","ABC0002");
        inputs.put("id","ABC0003");
        inputs.put("id","ABC0004");
        inputs.put("id","ABC0005");
        inputs.put("id","ABC0006");
        inputs.put("id","ABC0007");
        inputs.put("id","ABC0008");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方AB数据查询");
        inputs.clear();
        inputs.put("id","AB00001");
        inputs.put("id","AB00002");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方AC数据查询");
        inputs.clear();
        inputs.put("id","AC00002");
        inputs.put("id","AC00003");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("两方BC数据查询");
        inputs.clear();
        inputs.put("id","BC00001");
        inputs.put("id","BC00002");
        inputs.put("id","BC00003");
        inputs.put("id","BC00004");
        inputs.put("id","BC00005");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        log.info("单方A数据查询");
        inputs.clear();
        inputs.put("id","A000012");
        inputs.put("id","A000013");
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
        inputs.put("id","A000012");
        inputs.put("id","A000013");
        inputs.put("id","B方000003");
        inputs.put("id","B方000004");
        inputs.put("id","C方000001");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        log.info("多类型复杂数据查询");
        inputs.clear();
        inputs.put("id","ABC0009");
        inputs.put("id","ABC0010");
        inputs.put("id","AB00003");
        inputs.put("id","AC00001");
        inputs.put("id","BC00002");
        inputs.put("id","A000012");
        inputs.put("id","A000013");
        inputs.put("id","B方000003");
        inputs.put("id","B方000004");
        inputs.put("id","C方000001");
        response = sygt.SSSafeQueryDo(scene,label,inputs);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


        //获取查询结束后积分
        int pointA2 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");
        int pointB2 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");
        int pointC2 = JSONObject.fromObject(sygt.SSPointQuery(account1,effortPointType)).getJSONObject("data").getInt("balance");

        //需要根据积分策略最终确认校验值 暂定如下
        assertEquals(pointA1 + 10000,pointA2);
        assertEquals(pointB1 + 10000,pointB2);
        assertEquals(pointC1 + 10000,pointC2);
    }


}
