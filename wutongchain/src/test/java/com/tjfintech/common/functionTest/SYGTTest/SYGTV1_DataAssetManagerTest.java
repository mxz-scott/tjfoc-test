package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SYGTV1_DataAssetManagerTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();
    SYGTCommonFunc sygtCF = new SYGTCommonFunc();
    Boolean bCheckPoint = false;


    @Before
    public void updateSceneLables()throws Exception{
        code3 = "m3code" + UtilsClass.Random(6); //成员1
        name3 = "m3name" + UtilsClass.Random(6); //成员1

        List<Map> listScenes = new ArrayList<>();
        Map mapScene = new HashMap();
        Map mapScene2 = new HashMap();
        Map mapScene3 = new HashMap();
        mapScene.put("code","1");mapScene.put("name","反洗钱名单");      listScenes.add(mapScene);
        mapScene2.put("code","2");mapScene2.put("name","恶意投诉客户名单");listScenes.add(mapScene2);
        mapScene3.put("code","3");mapScene3.put("name","疑似倒买倒卖名单");listScenes.add(mapScene3);

        List<Map> listLabels = new ArrayList<>();
        Map mapLabel = new HashMap();
        Map mapLabel2 = new HashMap();
        mapLabel.put("code","4");mapLabel.put("name","高风险名单");listLabels.add(mapLabel);
        mapLabel2.put("code","5");mapLabel2.put("name","低风险名单");listLabels.add(mapLabel2);

        sygt.SSSettingUpdate(listScenes,listLabels);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
    }

    //执行后退出操作的成员
//    @After
    public void exitMember()throws Exception{
        sygtCF.memberExit(code3,"exit");
    }


    /**
     * 盟主1发布数据资产  盟主1 2 成员查看授权信息
     * 盟主1授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * 盟主1取消授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * @throws Exception
     */
    @Test
    public void TC01_assetPublishy011() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account, contributePointType);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //成员加入
//        sygtCF.memberJoin(code1,name1,endPoint1);
//
        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
        //盟主1发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }


        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURLm1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //查看可用资产  当前仅显示被授权的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));

        SDKADD = SDKURLm1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //获取积分  确认授权及取消不会变更积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL1;
        //取消授权 account2
        response = sygt.SSAssetCancelAuthority(assetID,account2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        sygtCF.checkAssetAuth(assetID,account2,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));


        SDKADD = SDKURL1;//设置为盟主1 SDK
        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  积分恢复到初始加入时的积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }


    /**
     * 多次发布 20
     * @throws Exception
     */
    @Test
    public void TC04_assetPublishyMulti02() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;
        ArrayList listAssetID = new ArrayList();

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account, contributePointType);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        for(int i = 0;i < 20;i++) {
            assetID = "asset" + Random(12);
            listAssetID.add(assetID);
            SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
            //发布资产
            response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(
                    store.GetTxDetail(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20))).getString("state"));

            //获取积分  具体积分变更待确认
            if(bCheckPoint) {
                response = sygt.SSPointQuery(account, contributePointType);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
                point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
                assertEquals(true, point > memberJoinPoint);
                assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            }

            //查看可用资产
            response = sygt.SSAssetQuery(scene, label);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));


            //查看授权情况
            sygtCF.checkAssetAuth(assetID, account1, false);
            sygtCF.checkAssetAuth(assetID, account2, false);
            sygtCF.checkAssetAuth(assetID, account3, false);


            SDKADD = SDKURL1;
            //资产授权 account2
            String authAccount = account2;
            String authID = "auth" + Random(10);
            String startDate = "2021-03-30 10:00:00";
            String endDate = "2022-03-30 10:00:00";
            response = sygt.SSAssetAuthorize(assetID, authAccount, authID, startDate, endDate);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(
                    store.GetTxDetail(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20))).getString("state"));


            //查看授权情况
            sygtCF.checkAssetAuth(assetID, account1, false);
            sygtCF.checkAssetAuth(assetID, account2, true);
            sygtCF.checkAssetAuth(assetID, account3, false);


            SDKADD = SDKURL2;
            //查看授权情况
            sygtCF.checkAssetAuth(assetID, account1, false);
            sygtCF.checkAssetAuth(assetID, account2, true);
            sygtCF.checkAssetAuth(assetID, account3, false);

            response = sygt.SSAssetQuery(scene, label);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(true, response.contains(assetID));

        }
    }


    /**
     * 多次发布 授权不同账户
     * @throws Exception
     */
    @Test
    public void TC03_assetPublishyMulti01() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account, contributePointType);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));


        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
        //发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //查看可用资产  当前仅显示被授权的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));

        SDKADD = SDKURL1;
        //再次发布
        String assetID2 = "assetMore" + Random(12);
        response = sygt.SSAssetPublish(assetID2,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID2,account1,false);
        sygtCF.checkAssetAuth(assetID2,account2,false);
        sygtCF.checkAssetAuth(assetID2,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));
        assertEquals(false, response.contains(assetID2));


        SDKADD = SDKURL1;
        //资产授权 account3 此处可能是因为重复加入过第三方 所以account3 是存在在成员列表中的
        authAccount = account3;
        String authID2 = "auth" + Random(10);
        response = sygt.SSAssetAuthorize(assetID2,authAccount,authID2,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID2,account1,false);
        sygtCF.checkAssetAuth(assetID2,account2,false);
        sygtCF.checkAssetAuth(assetID2,account3,true);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));
        assertEquals(false, response.contains(assetID2));

        SDKADD = SDKURL1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID2,account1,false);
        sygtCF.checkAssetAuth(assetID2,account2,false);
        sygtCF.checkAssetAuth(assetID2,account3,true);

        SDKADD = SDKURLm1;
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));
        assertEquals(true, response.contains(assetID2));
    }



    /**
     * 成员加入后发布数据资产
     * @throws Exception
     */
    @Test
    public void TC02_assetPublishy012() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3,name3,endPoint3,account3);

//        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //资产授权 code1
        String authAccount = account1;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,true);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        //资产授权 code2
        authAccount = account2;
        authID = "auth" + Random(10);
        startDate = "2021-03-30 10:00:00";
        endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,true);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        //查看可用资产  当前仅显示被授权的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));


        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //取消授权 code1
        response = sygt.SSAssetCancelAuthority(assetID,account1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);


        //获取积分  确认授权及取消不会变更积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //取消授权 code2
        response = sygt.SSAssetCancelAuthority(assetID,account2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURLm1;
        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  积分恢复到初始加入时的积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }

    /**
     *
     * 盟主1发布数据资产 盟主1 2 成员查看授权信息
     * 盟主1授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * 更新数据资产 盟主2查询可用资产
     * 盟主1取消授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * @throws Exception
     */
    @Test
    public void TC05_assetUpdate01() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }


        SDKADD = SDKURL1;//SDKURL1;  //SDK设置为盟主 SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        //不可查看自己的
        assertEquals(false, response.contains(assetID));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURLm1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //更新数据资产
        response = sygt.SSAssetUpdate(assetID,scene,label,amount + 100,scene + label + "update");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //查看可用资产  当前仅显示被授权的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));
        assertEquals(true, response.contains("\"qty\":" + (amount + 100)));

        SDKADD = SDKURLm1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //获取积分  确认授权及取消不会变更积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL1;
        //取消授权 account2
        response = sygt.SSAssetCancelAuthority(assetID,account2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        sygtCF.checkAssetAuth(assetID,account2,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));


        SDKADD = SDKURL1;//设置为盟主1 SDK
        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  积分恢复到初始加入时的积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }

    /**
     * 盟主1发布数据资产 更新 盟主1 2 成员查看授权信息
     * 盟主1授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * 盟主1取消授权给盟主2 数据资产 盟主1 2 成员查看授权信息 盟主2查询可用资产
     * @throws Exception
     */
    @Test
    public void TC06_assetUpdate02() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
//        成员加入
        sygtCF.memberJoin(code3,name3,endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL1;
        //发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //更新数据资产
        response = sygt.SSAssetUpdate(assetID,scene,label,amount + 100,scene + label + "update");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        //不可查看自己的
        assertEquals(false, response.contains(assetID));


        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //查看可用资产  当前仅显示被授权的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));
        assertEquals(true, response.contains("\"qty\":" + (amount + 100)));

        SDKADD = SDKURLm1;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        //获取积分  确认授权及取消不会变更积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL1;
        //取消授权 account2
        response = sygt.SSAssetCancelAuthority(assetID,account2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        sygtCF.checkAssetAuth(assetID,account2,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));


        SDKADD = SDKURL1;//设置为盟主1 SDK
        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  积分恢复到初始加入时的积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }

    /**
     * 资产重复授权
     * @throws Exception
     */
    @Test
    public void TCInvalid01_assetAuthDuplicate() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account, contributePointType);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
        //发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene, label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID, account1, false);
        sygtCF.checkAssetAuth(assetID, account2, false);
        sygtCF.checkAssetAuth(assetID, account3, false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID, authAccount, authID, startDate, endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20))).getString("state"));


        response = sygt.SSAssetAuthorize(assetID, authAccount, authID, startDate, endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20))).getString("state"));


        //查看授权情况
        sygtCF.checkAssetAuth(assetID, account1, false);
        sygtCF.checkAssetAuth(assetID, account2, true);
        sygtCF.checkAssetAuth(assetID, account3, false);


        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID, account1, false);
        sygtCF.checkAssetAuth(assetID, account2, true);
        sygtCF.checkAssetAuth(assetID, account3, false);

        response = sygt.SSAssetQuery(scene, label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));


    }


    /**
     * 下架不存在的数据资产
     * @throws Exception
     */
    @Test
    public void TCInvalid01_assetOffNotExist() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK

        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

    }


    /**
     * 授权不存在的数据资产
     * @throws Exception
     */
    @Test
    public void TCInvalid01_assetAuthorizeNotExist() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3,name3,endPoint3,account3);

//        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为成员SDK

        //资产授权 code2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

        SDKADD = SDKURL2;
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }
    /**
     * 非加入成员发布资产
     * 如果已经存在account的话 此用例可能测试会失败
     * @throws Exception
     */
    @Test
    public void TCInvalid01_assetPublishNotMember() throws Exception {
        String scene = "1";//"反洗钱名单";
        String label = "4";//"高风险名单";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account1;

        String response = "";

        SDKADD = SDKURL1;

        //获取初始积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals(200, JSONObject.fromObject(response).getString("state"));
        }

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }


    /**
     * 盟主发布数据资产 授权 下架 查询可用资产
     * @throws Exception
     */
    @Test
    public void TC07_assetOffQuery() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        if(bCheckPoint) {
            response = sygt.SSAssetQuery(scene, label);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            sygtCF.checkAsset(response, assetID, amount, scene + label, code1, name1, account1, endPoint1);
        }
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //资产授权 account2
        String authAccount = account2;
        String authID = "auth" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2022-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authAccount,authID,startDate,endDate);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        //查看授权情况
        //查询可用资产不返回自己发布的数据资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL2;
        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,true);
        sygtCF.checkAssetAuth(assetID,account3,false);

        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(assetID));


        SDKADD = SDKURL1;//设置为盟主1 SDK
        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  积分恢复到初始加入时的积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));


        SDKADD = SDKURL2;
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);

    }

    /***
     * 测试目的 检查场景和标签code不可以相同
     * @throws Exception
     */
    @Test
    public void TC08_sameCodeForSceneAndLabel()throws Exception{

        List<Map> listScenes = new ArrayList<>();
        Map mapScene = new HashMap();
        Map mapScene3 = new HashMap();
        mapScene.put("code","11");mapScene.put("name","反洗钱名单");      listScenes.add(mapScene);
        mapScene3.put("code","13");mapScene3.put("name","疑似倒买倒卖名单");listScenes.add(mapScene3);

        List<Map> listLabels = new ArrayList<>();
        Map mapLabel = new HashMap();
        Map mapLabel2 = new HashMap();
        mapLabel.put("code","11");mapLabel.put("name","高风险名单");listLabels.add(mapLabel);
        mapLabel2.put("code","15");mapLabel2.put("name","低风险名单");listLabels.add(mapLabel2);

        String response = sygt.SSSettingUpdate(listScenes,listLabels);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }

    /**
     * 发布数据资产后再次发布相同assetID的数据资产
     * @throws Exception
     */
    @Test
    public void TCInvalid01_assetPublishy02_SameAssetID() throws Exception {
        String scene = "1";//"反洗钱名单";
        String label = "4";//"高风险名单";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account1;

        String response = "";

        SDKADD = SDKURL1;

        //获取初始积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals(200, JSONObject.fromObject(response).getString("state"));
        }

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //再次使用相同assetID发布数据资产
        scene = "2";
        label = "5";
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }

    /**
     * 更新不存在的数据资产ID
     * @throws Exception
     */
    @Test
    public void TCInvalid01_updateAssetNotExist() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        String account = account3;

        String response = "";

        SDKADD = SDKURL1;
        //成员发布资产
        response = sygt.SSAssetUpdate(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }

    /**
     * 取消未授权的授权
     * @throws Exception
     */
    @Test
    public void TCInvalid01_cancelAuthorizeNotExist() throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 2;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        SDKADD = SDKURL1;//SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);


        SDKADD = SDKURL1;
        //取消授权 account2
        response = sygt.SSAssetCancelAuthority(assetID,account2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权情况
        sygtCF.checkAssetAuth(assetID,account1,false);
        sygtCF.checkAssetAuth(assetID,account2,false);
        sygtCF.checkAssetAuth(assetID,account3,false);
    }

    /**
     * 场景及标签检查
     * 测试全量更新
     * 测试更新后数据信息正确
     * 20210412 更新策略为 如全量更新时 存在的场景则更新 不存在的则新增
     * @throws Exception
     */
//    @Test
    public void checkScenesAndLabels()throws Exception{
        String response = "";
        response = sygt.SSSettingGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String scenesString = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("scenes").toString();
        String labelsString = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("labels").toString();
        assertEquals(3,JSONObject.fromObject(response).getJSONObject("data").getJSONArray("scenes").size());
        assertEquals(2,JSONObject.fromObject(response).getJSONObject("data").getJSONArray("labels").size());

        assertEquals(true,scenesString.contains("\"code\":\"1\",\"name\":\"反洗钱名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"2\",\"name\":\"恶意投诉客户名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"3\",\"name\":\"疑似倒买倒卖名单\""));
        assertEquals(true,labelsString.contains("\"code\":\"4\",\"name\":\"高风险名单\""));
        assertEquals(true,labelsString.contains("\"code\":\"5\",\"name\":\"低风险名单\""));


        //更新场景
        List<Map> listScenes = new ArrayList<>();
        Map mapScene = new HashMap();
        mapScene.put("code","5");mapScene.put("name","多头共债名单");listScenes.add(mapScene);
        mapScene.put("code","6");mapScene.put("name","欺诈账户名单");listScenes.add(mapScene);
        mapScene.put("code","7");mapScene.put("name","羊毛党名单");listScenes.add(mapScene);
        mapScene.put("code","8");mapScene.put("name","疑似配资客户");listScenes.add(mapScene);

        List<Map> listLabels = new ArrayList<>();
        Map mapLabel = new HashMap();
        mapLabel.put("code","1");mapLabel.put("name","高风险");listLabels.add(mapLabel);
        mapLabel.put("code","2");mapLabel.put("name","低风险");listLabels.add(mapLabel);
        mapLabel.put("code","9");mapLabel.put("name","中风险");listLabels.add(mapLabel);

        sygt.SSSettingUpdate(listScenes,listLabels);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查更新后的场景标签
        response = sygt.SSSettingGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        scenesString = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("scenes").toString();
        labelsString = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("labels").toString();
        assertEquals(4,JSONObject.fromObject(response).getJSONObject("data").getJSONArray("scenes").size());
        assertEquals(3,JSONObject.fromObject(response).getJSONObject("data").getJSONArray("labels").size());

        assertEquals(true,scenesString.contains("\"code\":\"5\",\"name\":\"多头共债名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"6\",\"name\":\"欺诈账户名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"7\",\"name\":\"羊毛党名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"8\",\"name\":\"疑似配资客户\""));
        assertEquals(true,labelsString.contains("\"code\":\"1\",\"name\":\"高风险\""));
        assertEquals(true,labelsString.contains("\"code\":\"2\",\"name\":\"低风险\""));
        assertEquals(true,labelsString.contains("\"code\":\"9\",\"name\":\"中风险\""));

        //测试全量更新
        assertEquals(true,scenesString.contains("\"code\":\"1\",\"name\":\"反洗钱名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"2\",\"name\":\"恶意投诉客户名单\""));
        assertEquals(true,scenesString.contains("\"code\":\"3\",\"name\":\"疑似倒买倒卖名单\""));
        assertEquals(true,labelsString.contains("\"code\":\"4\",\"name\":\"高风险名单\""));
        assertEquals(true,labelsString.contains("\"code\":\"5\",\"name\":\"低风险名单\""));
    }

    /***
     * 更新数与发布数相同
     * @throws Exception
     */
    @Test
    public void TC30_testPointUpdate01()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 256;
        int point = 2;
        String type = "credit";
        String pointType = contributePointType;
        String operationCode = memberAddCode;
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        point =  mapPlatformPoint.get(memberAddCode);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }
        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getInt("balance") > point);
            assertEquals(point + mapPlatformPoint.get(assetPublishCode), JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }

        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + mapPlatformPoint.get(operationCode)*mapPointOP.get(type),
                    JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }

        SDKADD = SDKURLm1;   //设置为成员 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //获取积分 积分不变更
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point,JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }
        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "debit";
        operationCode = assetUpdateCode;
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + mapPlatformPoint.get(operationCode)*mapPointOP.get(type),
                    JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }

        //更新积分 操作为自定义操作代码 credit(增加积分) 200
        type = "debit";
        operationCode = "notmention";
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point,JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

    }

    /***
     * 更新数多于发布数相同  更新数量移除
     * @throws Exception
     */
//    @Test
    public void TC31_testPointUpdate02()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 256;
        int point = 2;
        String type = "credit";
        String pointType = contributePointType;
        String operationCode = "A001";
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分)
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount + 100, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }


        SDKADD = SDKURLm1;   //设置为成员 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //获取积分 积分不变更
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount + 100, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "debit";
        operationCode = "M001";
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount + 100 - amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //更新积分 操作为自定义操作代码 credit(增加积分) 200
        type = "debit";
        operationCode = "notmention";
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount + 100 - amount * 2, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }

    /***
     * 更新数少于发布数相同 更新数量移除
     * @throws Exception
     */
//    @Test
    public void TC33testPointUpdate03()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 256;
        int point = 2;
        String type = "credit";
        String pointType = contributePointType;
        String operationCode = "A001";
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        }

        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分)
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount - 100, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }


        SDKADD = SDKURLm1;   //设置为成员 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //获取积分 积分不变更
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount - 100, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "debit";
        operationCode = "M001";
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount - 100 - amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }


        //更新积分 操作为自定义操作代码 credit(增加积分) 200
        type = "debit";
        operationCode = "notmention";
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + amount - 100 - amount * 2, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

    }

    /***
     * 更新不存在的资产ID
     * @throws Exception
     */
    @Test
    public void TC3Invalid01_testPointUpdate04()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 256;
        int point = 2;
        String type = "credit";
        String pointType = contributePointType;
        String operationCode = "A001";
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        }

        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分)
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }


    /***
     * 积分更新 余额不足
     * @throws Exception
     */
    @Test
    public void TC3Invalid_testPointUpdateNotEnough()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String operationCode = "A001";
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }

        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        }

        SDKADD = SDKURL1;
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "credit";
        operationCode = assetUpdateCode;
        response = sygt.SSPointUpdate(account, type, pointType, operationCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(point + mapPlatformPoint.get(operationCode) * mapPointOP.get(type),
                    JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }

    /***
     * 成员仍存在数据资产时退出
     * @throws Exception
     */
    @Test
    public void TC091_exitMemberWithDataAsset()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String operationCode = "A001";
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL1;   //设置为盟主1 SDK
        sygtCF.memberExit(code3,"exit");

        //获取积分  类型2
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

            //获取积分 类型1
            pointType = contributePointType;
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        response = sygt.SSMembersGet();
        mapMem.clear();
        mapMem.put("code",code3);
        mapMem.put("name",name3);
        mapMem.put("serviceEndpoint",endPoint3);
        mapMem.put("account",account3);
        mapMem.put("status",accStatusExitSuccess);
        mapMem.put("isLeader",false);
        mapMem.put("joinDate","");
        mapMem.put("isSelf",false);
        mapMem.put("joinCheckDetail",account1 + "," + account2);
        mapMem.put("exitCheckDetail",account1 + "," + account2);

        sygtCF.checkMemberInfo(response,mapMem);

        SDKADD = SDKURLm1;//自己查询自己
        response = sygt.SSMembersGet();
        mapMem.clear();
        mapMem.put("code",code3);
        mapMem.put("name",name3);
        mapMem.put("serviceEndpoint",endPoint3);
        mapMem.put("account",account3);
        mapMem.put("status",accStatusExitSuccess);
        mapMem.put("isLeader",false);
        mapMem.put("joinDate","");
        mapMem.put("isSelf",true);
        mapMem.put("joinCheckDetail",account1 + "," + account2);
        mapMem.put("exitCheckDetail",account1 + "," + account2);

        sygtCF.checkMemberInfo(response,mapMem);
    }


    /***
     * 成员仍存在某数据资产的授权时退出  当前预期可以退出 但会同时更新授权列表
     * 当前测试 会重复利用第三个账户 测试用例 无法正确有效执行
     * @throws Exception
     */
    @Test
    public void TC092_exitMemberWithAssetAuthority()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL1;  //SDK设置为盟主1 SDK
        //盟主发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分  具体积分变更待确认
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
            assertEquals(true, point > memberJoinPoint);
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        //授权给成员
        String authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account3,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        SDKADD = SDKURL1;   //设置为盟主1 SDK 退出成员
        sygtCF.memberExit(code3,"exit");

        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));
    }


    /***
     * 非资产拥有者授权资产 成员发布 盟主1授权盟主2
     * @throws Exception
     */
    @Test
    public void TCInvalid01_authorizeNotOwner01()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为盟主1 SDK
        //盟主1 发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;  //SDK设置为盟主2 SDK
        //盟主2 授权给成员
        String authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account1,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;  //SDK设置为盟主2 SDK
        //盟主2 授权给成员
        authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account2,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        //正常授权
        SDKADD = SDKURLm1;
        //盟主1 授权给成员
        authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account1,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        

        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

    }

    /***
     * 授权一个未添加的服务
     * @throws Exception
     */
    @Test
    public void TCInvalid01_authorizeNotExistServiceID()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为盟主1 SDK
        //盟主1 发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        SDKADD = SDKURL2;  //SDK设置为盟主2 SDK
        //盟主2 授权给成员
        String authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account1,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //添加服务ID后再次授权
        String serviceID = "service" + Random(3);
        response = sygt.SSAssetService(assetID,serviceID,authID);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        SDKADD = SDKURL2;
        response = sygt.SSAssetQuery(scene,label);
        assertEquals(true,response.contains(assetID));

    }


    /***
     * 非资产拥有者授权资产 盟主1发布 盟主2成员  成员授权盟主2
     * @throws Exception
     */
    @Test
    public void TCInvalid01_authorizeNotOwner02()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURL1;  //SDK设置为盟主1 SDK
        //盟主1 发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;  //SDK设置为盟主2 SDK
        //盟主2 授权给成员
        String authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account3,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURLm1;  //SDK设置为成员 SDK
        //成员授权给盟主2
        authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account2,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        //正常授权
        SDKADD = SDKURL1;
        //盟主1 授权给成员
        authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account2,authID,"2021-03-12 12:00:00","2022-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

    }

    /***
     * 授权起始时间为未来 过去
     * @throws Exception
     */
    @Test
    public void TCInvalid01_authorizeInFutureDate()throws Exception {
        String scene = "1";
        String label = "4";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 2;
        String type = "debit";
        String pointType = platformPoint;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String account = account3;

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //成员加入
        sygtCF.memberJoin(code3, name3, endPoint3,account3);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account, pointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        SDKADD = SDKURLm1;  //SDK设置为成员 SDK
        //成员 发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //正常授权
        SDKADD = SDKURLm1;  //SDK设置为成员
        //成员 授权给盟主1  过去时间
        String authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account1,authID,"2001-03-02 12:00:00","2002-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //成员 授权给盟主2 将来时间
        authID = "authID" + Random(3);
        response = sygt.SSAssetAuthorize(assetID,account2,authID,"2031-03-02 12:00:00","2032-03-12 12:00:00");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //查看授权列表
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

    }


}
