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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassTap.*;
import static org.hamcrest.Matchers.*;
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
    TapCommonFunc tapCommonFunc = new TapCommonFunc();

    String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));

    @BeforeClass
    public static void init() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();

        TapCommonFunc tapCommonFunc = new TapCommonFunc();
        tapCommonFunc.init();
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
        expireDate = System.currentTimeMillis() / 1000 + 20;
        openDate = System.currentTimeMillis() / 1000 + 20;
        String response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, name, metaData);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String projectid = JSONObject.fromObject(response).getJSONObject("data").getString("projectId");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");

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
                (projectid, expireDate + 10, openDate + 20, metaDataNew, name + "update", stateSuspend, filesizeNew, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        response = tap.tapProjectDetail(projectid);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(expireDate + 10, JSONObject.fromObject(response).getJSONObject("data").getLong("expireDate"));
        assertEquals(openDate + 20, JSONObject.fromObject(response).getJSONObject("data").getLong("openDate"));
        assertEquals(filesizeNew, JSONObject.fromObject(response).getJSONObject("data").getInt("filesize"));
        assertEquals(name + "update", JSONObject.fromObject(response).getJSONObject("data").getString("name"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getString("metaData").contains("old"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("metaData").contains("update"));
        assertEquals(stateSuspend, JSONObject.fromObject(response).getJSONObject("data").getInt("state"));

        //该项目更新为正常状态
        response = tap.tapProjectUpdate
                (projectid, expireDate + 20, openDate + 20, metaDataNew, name + "update", stateNormal, filesizeNew, sign);
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

        //获取投标信息列表,获取投标列表接口请求时间大于开标时间
        assertThat(System.currentTimeMillis() / 1000,lessThan(openDate+20));
        response = tap.tapTenderRecord(projectid, recordIdA, true, sign);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("OpenDate is later"));
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(projectid, recordIdA, true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(recordIdA, com.alibaba.fastjson.JSONObject.parseObject(
                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
                        "data").getString("RecordInfos")).get(0).toString()).getString("recordId"));

        //开标
        response = tap.tapTenderOpen(projectid, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapProjectDetail(projectid);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(3, JSONObject.fromObject(response).getJSONObject("data").getInt("state"));
    }

    /**
     * 异常流程测试-投标文件上传接口
     * 项目标识为1-暂停、2-流标、3-已结束的项目，不可以再上传投标文件
     */
    @Test
    public void tapTenderUploadInvalidTest() throws Exception {

        String projectid = tapCommonFunc.initProject();

        //项目标识projectId为1-暂停的项目
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectid, "");
        String response = tap.tapProjectUpdate(projectid, expireDate, openDate, metaData, name, stateSuspend, filesize, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("bid state is abnormal"));

        //项目标识projectId为2-流标的项目
        response = tap.tapProjectUpdate(projectid, expireDate, openDate, metaData, name, stateAbortivebid, filesize, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("bid state is abnormal"));

        //项目标识projectId为3-开标已结束的项目
        response = tap.tapProjectUpdate(projectid, expireDate, openDate, metaData, name, stateNormal, filesize, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(30 * 1000);
        response = tap.tapTenderOpen(projectid, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("bid state is abnormal"));
    }

    /**
     * 异常流程测试-招标信息更新接口
     * 项目标识3-已结束的项目，不可以再更新招标信息
     */
    @Test
    public void tapProjectUpdateInvalidTest() throws Exception {

        String projectid = tapCommonFunc.initProject();

        //项目标识projectId为3-开标已结束的项目
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectid, "");
        sleepAndSaveInfo(30 * 1000);
        String response = tap.tapTenderOpen(projectid, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapProjectUpdate
                (projectid, expireDate + 50, openDate + 100, metaDataNew, name + "update", stateSuspend, filesizeNew, sign);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("can not modify finished bid"));
    }

    /**
     * 获取招标信息接口
     * 只返回状态为0-正常的招标项目信息
     * 不返回1-暂停；2-流标；3-已结束的招标项目信息
     */
    @Test
    public void tapProjectListTest() throws Exception {

        String projectid = tapCommonFunc.initProject();
        String projectid1 = tapCommonFunc.initProject();
        String projectid2 = tapCommonFunc.initProject();
        String projectid3 = tapCommonFunc.initProject();

        //变更项目状态projectid1（1-暂停）、projectid2（2-流标）、projectid3（3-已结束）
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectid, "");
        String sign1 = certTool.tapSign(sdkIP, PRIKEY1, "", projectid1, "");
        String sign2 = certTool.tapSign(sdkIP, PRIKEY1, "", projectid2, "");
        String sign3 = certTool.tapSign(sdkIP, PRIKEY1, "", projectid3, "");

        String response = tap.tapProjectUpdate(projectid1, 0, 0, metaData, name, stateSuspend, filesize, sign1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapProjectUpdate(projectid2, 0, 0, metaData, name, stateAbortivebid, filesize, sign2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(30 * 1000);
        response = tap.tapTenderOpen(projectid3, sign3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(projectid));
        assertEquals(false, response.contains(projectid1));
        assertEquals(false, response.contains(projectid2));
        assertEquals(false, response.contains(projectid3));
    }

    /**
     * 获取投标信息列表接口
     * 同一个投标标识可以多次上传标书，查询接口正常
     * detail不传或者传值false返回指定信息，传值true返回详细信息
     * recordId传值返回指定投标信息，不传返回所有投标信息
     */
    @Test
    public void tapTenderRecordTest() throws Exception {

        String projectid = tapCommonFunc.initProject();
        String projectid1 = tapCommonFunc.initProject();
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectid, "");
        String sign1 = certTool.tapSign(sdkIP, PRIKEY1, "", projectid1, "");

        //投标文件上传
        String response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(projectid, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapTenderUpload(projectid1, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用projectid1查询
        sleepAndSaveInfo(20*1000);
        response = tap.tapTenderRecord(projectid1, recordIdB, true, sign1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        response = tap.tapTenderRecord(projectid1, "", false, sign1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId数据不存在
        response = tap.tapTenderRecord(projectid, "123456", true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains("123456"));

        //recordId传值存在的数据recordIdA、detail传值true
        response = tap.tapTenderRecord(projectid, recordIdA, true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(false, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordId传值存在的数据recordIdB、detail传值false
        response = tap.tapTenderRecord(projectid, recordIdB, false, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId传值存在的数据recordIdB、detail传值null
        response = tap.tapTenderRecord(projectid, recordIdB, null, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail传值true
        response = tap.tapTenderRecord(projectid, "", true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordI为空、detail传值false
        response = tap.tapTenderRecord(projectid, "", false, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail为空
        response = tap.tapTenderRecord(projectid, "", null, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

    }

    /**
     * 获取投标信息列表接口
     * 解密filePath数据
     */
    @Test
    public void tapDecryptPathTest() throws Exception {

        String projectid = tapCommonFunc.initProject();
        //投标文件上传
        String response = tap.tapTenderUpload(projectid, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用projectId查询
        sleepAndSaveInfo(20*1000);
        response = tap.tapTenderRecord(projectid, recordIdA, true, sign);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        String RecordInfos = com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject("data").getString("RecordInfos");
        Object RecordInfo= com.alibaba.fastjson.JSONObject.parseArray(RecordInfos).get(0);
        String filePath= JSONObject.fromObject(RecordInfo).getString("filePath");
        String keySecret= JSONObject.fromObject(RecordInfo).getString("keySecret");
        assertEquals(recordIdA, JSONObject.fromObject(RecordInfo).getString("recordId"));

        String decryptFilePath = certTool.tapDecryptFilePath(sdkIP,"","",filePath,keySecret);
        assertEquals(true,decryptFilePath.contains(path));

    }


}