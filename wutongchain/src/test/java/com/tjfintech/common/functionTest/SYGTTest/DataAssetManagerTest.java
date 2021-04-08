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
import org.springframework.util.StringUtils;

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
public class DataAssetManagerTest {

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
     * 成员加入后发布数据资产
     * @throws Exception
     */
    @Test
    public void assetPublishy01() throws Exception {
        String scene = "反洗钱名单";
        String label = "高风险名单";
        String assetID = "asset" + Random(12);
        int amount = 123456;
        int point = 0;

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account3,effortPointType);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        //成员加入
        sygtCF.memberJoin(code1,name1,endPoint1);

        //获取积分
        response = sygt.SSPointQuery(account3,effortPointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取积分  具体积分变更待确认
        response = sygt.SSPointQuery(account3,effortPointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        assertEquals(true,point > memberJoinPoint);
//        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        sygtCF.checkAsset(response,assetID,amount,scene+label,code1,name1,account1,endPoint1);

        //查看授权情况
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //资产授权 code1
        String authCode = code1;
        String serviceID = "service" + Random(10);
        String startDate = "2021-03-30 10:00:00";
        String endDate = "2021-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authCode,serviceID,startDate,endDate);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        //查看授权情况
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        //资产授权 code2
        authCode = code2;
        serviceID = "service" + Random(10);
        startDate = "2021-03-30 10:00:00";
        endDate = "2021-03-30 10:00:00";
        response = sygt.SSAssetAuthorize(assetID,authCode,serviceID,startDate,endDate);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        //查看授权情况
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        //取消授权 code1
        response = sygt.SSAssetCancelAuthority(assetID,code1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查看授权情况
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        //获取积分  确认授权及取消不会变更积分
        response = sygt.SSPointQuery(account3,effortPointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point,JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        //取消授权 code2
        response = sygt.SSAssetCancelAuthority(assetID,code2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //查看授权情况
        response = sygt.SSAssetVeriryAuthority(assetID,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account2);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

        response = sygt.SSAssetVeriryAuthority(assetID,account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));


        SDKADD = SDKURL1;//设置为盟主1 SDK

        //下架数据资产
        response = sygt.SSAssetOff(assetID);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取积分  积分恢复到初始加入时的积分
        response = sygt.SSPointQuery(account3,effortPointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));

    }

    /**
     * 场景及标签检查
     * 测试全量更新
     * 测试更新后数据信息正确
     * @throws Exception
     */
    @Test
    public void checkScenesAndLabels()throws Exception{
        String response = "";
        response = sygt.SSSettingGet();
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
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
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
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
        assertEquals(false,scenesString.contains("\"code\":\"1\",\"name\":\"反洗钱名单\""));
        assertEquals(false,scenesString.contains("\"code\":\"2\",\"name\":\"恶意投诉客户名单\""));
        assertEquals(false,scenesString.contains("\"code\":\"3\",\"name\":\"疑似倒买倒卖名单\""));
        assertEquals(false,labelsString.contains("\"code\":\"4\",\"name\":\"高风险名单\""));
        assertEquals(false,labelsString.contains("\"code\":\"5\",\"name\":\"低风险名单\""));
    }

    @Test
    public void testPointUpdate()throws Exception {
        String scene = "反洗钱名单";
        String label = "高风险名单";
        String assetID = "asset" + Random(12);
        int amount = 200;
        int point = 0;
        String type = "credit";
        int pointType = effortPointType;
        String operationCode = "A001";

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        //成员加入
        sygtCF.memberJoin(code1, name1, endPoint1);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分  具体积分变更待确认
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        assertEquals(true, point > memberJoinPoint);
//        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));


        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));


        SDKADD = SDKURLm1;   //设置为成员 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //获取积分 积分不变更
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "debit";
        operationCode = "M001";
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount - amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));


        //更新积分 操作为自定义操作代码 credit(增加积分) 200
        type = "debit";
        operationCode = "notmention";
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount - amount * 2, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

    }


    @Test
    public void testPointUpdateNotEnough()throws Exception {
        String scene = "反洗钱名单";
        String label = "高风险名单";
        String assetID = "asset" + Random(12);
        int amount = 200000;
        int point = 0;
        String type = "debit";
        int pointType = 2;//创始盟主 成员加入、退出 数据资产发布、下架 为变更贡献积分；查询及被查则变更平台交易积分
        String operationCode = "A001";

        String response = "";

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        //成员加入
        sygtCF.memberJoin(code1, name1, endPoint1);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID, scene, label, amount, scene + label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分  具体积分变更待确认
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        point = JSONObject.fromObject(response).getJSONObject("data").getInt("balance");
        assertEquals(true, point > memberJoinPoint);
//        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));


        SDKADD = SDKURL1;   //设置为盟主1 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));


        SDKADD = SDKURLm1;   //设置为成员 SDK
        //更新积分 A001 credit(增加积分) 200
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //获取积分 积分不变更
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURL2;   //设置为盟主2 SDK
        //更新积分 A001 credit(增加积分) 200
        type = "credit";
        operationCode = "M001";
        response = sygt.SSPointUpdate(account3, type, pointType, operationCode, amount);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取积分
        response = sygt.SSPointQuery(account3,pointType);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(point + amount, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
    }

}
