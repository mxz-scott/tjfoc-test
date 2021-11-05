package com.tjfintech.common.functionTest.Tap;

import com.alibaba.fastjson.JSONArray;
import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.checkerframework.checker.units.qual.C;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassTap.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TapProcessTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Tap tap = testBuilder.getTap();
    CertTool certTool = new CertTool();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));

    @BeforeClass
    public static void init() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
    }

    /**
     * 正常流程测试
     * 招标初始化-查询-更新-查询
     * 投标文件校验-投标文件上传-撤销投标
     * 获取招标信息列表-获取投标信息列表-开标
     */
    @Test
    public void tapProcessTest() throws Exception {

        //招标信息初始化
        publicKey = certTool.tapPubToHex(sdkIP, PRIKEY1, "", "", "");
        String response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, name, metaData);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String projectid = JSONObject.fromObject(response).getJSONObject("data").getString("projectId");

        //招标信息查询比对
        response = tap.tapProjectDetail(projectid);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(expireDate, JSONObject.fromObject(response).getJSONObject("data").getLong("expireDate"));
        assertEquals(openDate, JSONObject.fromObject(response).getJSONObject("data").getLong("openDate"));
        assertEquals(identity, JSONObject.fromObject(response).getJSONObject("data").getString("identity"));
        assertEquals(filesize, JSONObject.fromObject(response).getJSONObject("data").getInt("filesize"));
        assertEquals(name, JSONObject.fromObject(response).getJSONObject("data").getString("name"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("metaData").contains("old"));
        assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("state"));

        //招标信息更新，比对更新后的信息
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectid, "");
        response = tap.tapProjectUpdate
                (projectid, expireDate + 100, openDate + 100, metaDataNew, name + "update", stateSuspend, filesizeNew, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        response = tap.tapProjectDetail(projectid);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(expireDate + 100, JSONObject.fromObject(response).getJSONObject("data").getLong("expireDate"));
        assertEquals(openDate + 100, JSONObject.fromObject(response).getJSONObject("data").getLong("openDate"));
        assertEquals(filesizeNew, JSONObject.fromObject(response).getJSONObject("data").getInt("filesize"));
        assertEquals(name + "update", JSONObject.fromObject(response).getJSONObject("data").getString("name"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getString("metaData").contains("old"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("metaData").contains("update"));
        assertEquals(stateSuspend, JSONObject.fromObject(response).getJSONObject("data").getInt("state"));

        //该项目更新为正常状态
        response = tap.tapProjectUpdate
                (projectid, expireDate + 100, openDate + 100, metaDataNew, name + "update", stateNormal, filesizeNew, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //投标文件合规性校验
        response = tap.tapTenderVerify("123", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderVerify("123", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        response = tap.tapTenderVerify("123--", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //投标文件上传
        response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(projectid, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //撤销投标接口
        response = tap.tapTenderRevoke(metaData.toString(), projectid);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取招标信息列表
        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(projectid));

        //获取投标信息列表
        response = tap.tapTenderRecord(projectid, recordIdA, true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(recordIdA,com.alibaba.fastjson.JSONObject.parseObject(
                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
                        "data").getString("RecordInfos")).get(0).toString()).getString("recordId"));

    }


}