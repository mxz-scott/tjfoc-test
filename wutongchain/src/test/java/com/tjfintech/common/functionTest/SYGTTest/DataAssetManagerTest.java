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
        mapLabel.put("code","1");mapLabel.put("name","高风险名单");listLabels.add(mapLabel);
        mapLabel.put("code","2");mapLabel.put("name","低风险名单");listLabels.add(mapLabel);

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
        response = sygt.SSPointQuery(account3);
        assertEquals(400, JSONObject.fromObject(response).getString("state"));

        //成员加入
        sygtCF.memberJoin(code1,name1,endPoint1);

        //获取积分
        response = sygt.SSPointQuery(account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        SDKADD = SDKURLm1;  //SDK设置为成员SDK
        //成员发布资产
        response = sygt.SSAssetPublish(assetID,scene,label,amount,scene + label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取积分  具体积分变更待确认
        response = sygt.SSPointQuery(account3);
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
        response = sygt.SSPointQuery(account3);
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
        response = sygt.SSPointQuery(account3);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        //查看可用资产
        response = sygt.SSAssetQuery(scene,label);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(assetID));



    }

}
