package com.tjfintech.common.functionTest.Tap;

import com.alibaba.fastjson.JSONArray;
import com.gmsm.utils.GmUtils;
import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfoc.base.Util;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
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
        BID_DOC_REFER_END_TIME = constructTime(20000);
        KAIBIAODATE = constructTime(20000);
        BID_SECTION_CODE = constructData("SC");
        BID_SECTION_CODE_EX = constructData("SC_EX/+==");
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", TBFILE_ALLOWLIST, TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(BID_SECTION_CODE_EX, JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO"));
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String orderNo = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");

        //招标信息查询比对
        String orderNoURL = URLEncoder.encode(URLEncoder.encode(orderNo));
        response = tap.tapProjectDetail(orderNoURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(TENDER_PROJECT_CODE, JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_CODE"));
        assertEquals(TENDER_PROJECT_NAME, JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_NAME"));
        assertEquals(BID_SECTION_NAME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_NAME"));
        assertEquals(BID_SECTION_CODE, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE"));
        assertEquals(BID_DOC_REFER_END_TIME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_DOC_REFER_END_TIME"));
        assertEquals(KAIBIAODATE, JSONObject.fromObject(response).getJSONObject("data").getString("KAIBIAODATE"));
        assertEquals("1", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
        assertEquals(TBFILE_ALLOWLIST, JSONObject.fromObject(response).getJSONObject("data").getString("TBFILE_ALLOWLIST"));
        assertEquals(TBALLOWFILESIZE, JSONObject.fromObject(response).getJSONObject("data").getInt("TBALLOWFILESIZE"));
        assertEquals("1.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBTOOL_ALLOWVERSION"));
        assertEquals("1.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILEVERSION"));
        assertEquals(BID_SECTION_CODE_EX, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE_EX"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("EXTRA").contains("old"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").toString().contains("INIT_DATE"));

        //招标信息更新，比对更新后的信息
        BID_DOC_REFER_END_TIME = constructTime(30000);
        KAIBIAODATE = constructTime(30000);
        response = tap.tapProjectUpdate(orderNo, TENDER_PROJECT_CODE + "NEW", TENDER_PROJECT_NAME + "NEW",
                BID_SECTION_NAME + "NEW", BID_SECTION_CODE + "NEW", KAIBIAODATE, BID_DOC_REFER_END_TIME,
                "0", TBFILE_ALLOWLIST + "NEW", TBALLOWFILESIZENew, "2.0",
                "2.0", BID_SECTION_CODE_EX + "NEW", EXTRANew);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        response = tap.tapProjectDetail(orderNoURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(TENDER_PROJECT_CODE + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_CODE"));
        assertEquals(TENDER_PROJECT_NAME + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_NAME"));
        assertEquals(BID_SECTION_NAME + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_NAME"));
        assertEquals(BID_SECTION_CODE + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE"));
        assertEquals(BID_DOC_REFER_END_TIME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_DOC_REFER_END_TIME"));
        assertEquals(KAIBIAODATE, JSONObject.fromObject(response).getJSONObject("data").getString("KAIBIAODATE"));
        assertEquals("0", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
        assertEquals(TBFILE_ALLOWLIST + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILE_ALLOWLIST"));
        assertEquals(TBALLOWFILESIZENew, JSONObject.fromObject(response).getJSONObject("data").getInt("TBALLOWFILESIZE"));
        assertEquals("2.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBTOOL_ALLOWVERSION"));
        assertEquals("2.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILEVERSION"));
        assertEquals(BID_SECTION_CODE_EX + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE_EX"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("EXTRA").contains("update"));

        //该项目更新为正常状态
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "1", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //投标文件合规性校验
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "aaa@@bbb@@cccc@@ddddd@eeeeee",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        commonFunc.verifyTxDetailField(JSONObject.fromObject(response).getJSONObject("data").getString("txId"),
                "", "2", "3", "42");

        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "aaa", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "abc", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //投标文件上传
        String UIDA = constructData("UIDA");
        String UIDB = constructData("UIDB");
        String UIDC = constructData("UIDC");
        response = tap.tapTenderUpload(orderNo, UIDA, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(orderNo, UIDB, recordIdB, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(orderNo, UIDC, recordIdC, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        commonFunc.verifyTxDetailField(JSONObject.fromObject(response).getJSONObject("data").getString("txId"),
                "", "2", "3", "42");

        //撤销投标接口,撤销A和B投标记录,撤销时间之后再次重新上传一份投标文件
        String revokeData = "{\"unitname\":\"" + recordIdA + "\",\"revoketime\":\"" + constructTime(0) + "\"}";
        response = tap.tapTenderRevoke(revokeData, orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String revokeDataB = "{\"unitname\":\"" + recordIdB + "\",\"revoketime\":\"" + constructTime(0) + "\"}";
        response = tap.tapTenderRevoke(revokeDataB, orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        commonFunc.verifyTxDetailField(JSONObject.fromObject(response).getJSONObject("data").getString("txId"),
                "", "2", "3", "42");
        String NEWUIDA = constructData("NEWUIDA");
        response = tap.tapTenderUpload(orderNo, NEWUIDA, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //获取招标信息列表
        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(orderNo));

        //获取投标记录
        response = tap.tapTenderBack(UIDA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderBack(UIDB);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderBack(UIDC);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderBack(NEWUIDA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //获取投标信息列表,获取投标列表接口请求时间大于开标时间
        assertThat((Long.parseLong(constructTime(0))), lessThan(Long.parseLong(KAIBIAODATE)));
        response = tap.tapTenderRecord(orderNo, recordIdA, true);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("KAIBIAODATE is later"));
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(orderNo, "", true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(false, response.contains(recordIdB));
        assertEquals(true, response.contains(recordIdC));
//        assertEquals(recordIdB, com.alibaba.fastjson.JSONObject.parseObject(
//                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
//                        "data").getString("RecordInfos")).get(0).toString()).getString("recordId"));
//        assertEquals("1", com.alibaba.fastjson.JSONObject.parseObject(
//                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
//                        "data").getString("RecordInfos")).get(0).toString()).getInteger("version").toString());

        //开标,再次调更新接口，更新状态为5
        response = tap.tapTenderOpen(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.verifyTxDetailField(JSONObject.fromObject(response).getJSONObject("data").getString("txId"),
                "", "2", "3", "42");
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapProjectDetail(orderNoURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("1", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));

        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null);

        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapProjectDetail(orderNoURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("5", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
    }


    /**
     * 异常流程测试-招标信息更新接口
     * 状态不可以更新为除0和1外其他值
     * 项目标识5-已开标的项目，不可以再更新招标信息
     */
    @Test
    public void tapProjectUpdateInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //更新状态为3，接口返回错误
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "3", "", 0, "",
                "", "", null);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Illegal BID_SECTION_STATUS"));

        //更新状态为5-开标已结束的项目，再更新项目信息报错
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectUpdate(orderNo, TENDER_PROJECT_CODE + "NEW", TENDER_PROJECT_NAME + "NEW",
                BID_SECTION_NAME + "NEW", BID_SECTION_CODE + "NEW", KAIBIAODATE, BID_DOC_REFER_END_TIME,
                "0", TBFILE_ALLOWLIST + "NEW", TBALLOWFILESIZENew, "2.0",
                "2.0", BID_SECTION_CODE_EX + "NEW", EXTRANew);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("can not modify finished bid"));
    }

    /**
     * 异常流程测试-投标文件上传接口
     * 项目标识为0-异常、5-开标的项目，不可以再上传投标文件
     * 文件上传时间大于开标时间或者文件上传截止时间，不可以上传投标文件
     */
    @Test
    public void tapTenderUploadInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //项目标识orderNo为0-异常的项目
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, UID, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));

        //项目标识orderNo为5-开标的项目
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, UID, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));

        orderNo = tapCommonFunc.initProject();
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", constructTime(300000), constructTime(200000),
                "", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //
        response = tap.tapTenderUpload(orderNo, UID, recordIdA, fileHead, path, constructUnixTime(250));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("开标时间已经截止"));

        response = tap.tapTenderUpload(orderNo, UID, recordIdA, fileHead, path, constructUnixTime(350));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("开标时间已经截止"));

    }


    /**
     * 正常流程测试-获取招标信息接口
     * 只返回状态为1-正常的招标项目信息
     * 不返回0-异常；5-开标的招标项目信息
     */
    @Test
    public void tapProjectListTest() throws Exception {

        String orderNo0 = tapCommonFunc.initProject();
        String orderNo1 = tapCommonFunc.initProject();
        String orderNo5 = tapCommonFunc.initProject();

        //变更项目状态orderNo0（0-异常）、orderNo1（1-正常）、orderNo5（5-已开标）
        String response = tap.tapProjectUpdate(orderNo0, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectUpdate(orderNo5, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(orderNo1));
        assertEquals(false, response.contains(orderNo0));
        assertEquals(false, response.contains(orderNo5));

    }

    /**
     * 正常流程测试-获取投标信息列表接口
     * 同一个投标标识可以多次上传标书，查询接口正常
     * detail不传或者传值false返回指定信息，传值true返回详细信息
     * recordId传值返回指定投标信息，不传返回所有投标信息
     */
    @Test
    public void tapTenderRecordTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();
        String orderNo1 = tapCommonFunc.initProject();

        //投标文件上传
        String response = tap.tapTenderUpload(orderNo, UID, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(orderNo, UID, recordIdB, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapTenderUpload(orderNo1, UID, recordIdB, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用orderNo1查询
        sleepAndSaveInfo(30 * 1000);
        response = tap.tapTenderRecord(orderNo1, recordIdB, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        response = tap.tapTenderRecord(orderNo1, "", false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId数据不存在
        response = tap.tapTenderRecord(orderNo, "123456", true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains("123456"));

        //recordId传值存在的数据recordIdA、detail传值true
        response = tap.tapTenderRecord(orderNo, recordIdA, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(false, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordId传值存在的数据recordIdB、detail传值false
        response = tap.tapTenderRecord(orderNo, recordIdB, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId传值存在的数据recordIdB、detail传值null
        response = tap.tapTenderRecord(orderNo, recordIdB, null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail传值true
        response = tap.tapTenderRecord(orderNo, "", true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordI为空、detail传值false
        response = tap.tapTenderRecord(orderNo, "", false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail为空
        response = tap.tapTenderRecord(orderNo, "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

    }

    /**
     * 正常流程测试-获取投标信息列表接口
     * 同一个recordId和orderNo再次上传投标文件，获取列表的版本version+1
     * 解密filePath数据,数据为最后更新的path数据
     */
    @Test
    public void tapDecryptPathTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //投标文件上传
        String response = tap.tapTenderUpload(orderNo, constructData("UID1"), recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, constructData("UID2"), recordIdA, fileHead, "top/sub11/sub22/sub33", constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, constructData("UID3"), recordIdA, fileHead, "top/sub111/sub222/sub333", constructUnixTime(0));
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用orderNo查询
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(orderNo, recordIdA, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        String RecordInfos = com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject("data").getString("RecordInfos");
        Object RecordInfo = com.alibaba.fastjson.JSONObject.parseArray(RecordInfos).get(0);
        String filePath = JSONObject.fromObject(RecordInfo).getString("filePath");
        String keySecret = JSONObject.fromObject(RecordInfo).getString("keySecret");
        assertEquals(recordIdA, JSONObject.fromObject(RecordInfo).getString("recordId"));
        assertEquals(3, JSONObject.fromObject(RecordInfo).getInt("version"));

//        String decryptFilePath = certTool.tapDecryptFilePath(sdkIP, ZBRPRIKEY, "", filePath, keySecret);
        GmUtils gmUtils=new GmUtils();
        byte[] filePathByte = filePath.getBytes();
        String key = "abcdefgh#@&&1234";
        byte[] keyByte = key.getBytes();
        String keyHex = Util.byteToHex(keyByte);
        byte[] filePathByteSm4Decrypt = gmUtils.sm4Decrypt(keyHex,filePathByte);
        String decryptFilePath = Util.byteToString(filePathByteSm4Decrypt);
        assertEquals(false, decryptFilePath.contains(path));
        assertEquals(true, decryptFilePath.contains("top/sub111/sub222/sub333"));

    }

    /**
     * 异常流程测试-开标接口
     * 状态为5和0的项目不可以调开标接口
     */
    @Test
    public void tapTenderOpenInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //项目标识projectId为5-开标的项目
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(30 * 1000);
        response = tap.tapTenderOpen(orderNo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));

        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderOpen(orderNo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));
    }

    /**
     * 正常流程测试-初始化接口
     * 相同的项目不同的标段初始化生成两个项目标识
     * 相同的标段编号重复初始化，返回历史链上的项目标识和交易哈希
     */
    @Test
    public void tapProjectInitTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        String BID_SECTION_CODE2 = "SC2" + UtilsClass.Random(8);
        String BID_SECTION_NAME2 = "标段2" + UtilsClass.Random(8);
        String response2 = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME2, BID_SECTION_CODE2, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, constructData("SC_EX2/+=="), EXTRA);
        assertEquals("200", JSONObject.fromObject(response2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String orderNo2 = JSONObject.fromObject(response2).getJSONObject("data").getString("ORDERNO");
        String response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response2).getString("state"));
        assertThat(response, allOf(containsString(orderNo), containsString(orderNo2), containsString(BID_SECTION_CODE), containsString(BID_SECTION_CODE2),
                containsString(TENDER_PROJECT_CODE)));

        response = tap.tapProjectInit("code123456789", TENDER_PROJECT_NAME, BID_SECTION_NAME2, BID_SECTION_CODE2, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals(response2, response);
        assertEquals(false, response.contains("code123456789"));
    }

    /**
     * 异常流程测试-投标文件合规性校验接口
     * UserIdentifier_B不为空，CAType需匹配234
     * UserIdentifier_C不为空，CAType需匹配127
     * 文件平台调用，项目或者投标人不存在，校验失败
     */
    @Test
    public void tapTenderVerifyTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //招标平台调用传值userIdentifier_B，文件平台调用CAType传127，校验失败
        String response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "aaa@@bbb@@cccc@@ddddd@eeeeee",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "aaa", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("贵单位已在系统中注册电子营业执照信息，不允许上传使用CA锁生成的文件"));

        //招标平台调用更新userIdentifier_B和useZBFileGuid数据
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "fff@@gggg@@hhh@@iiiii",
                "", "", "useZBFileGuidNew", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //文件平台调用userIdentifier传参不等于userIdentifier_B其一，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "aaa", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("制作此投标文件的电子营业执照证书未在本系统中激活"));

        //文件平台调用userIdentifier不匹配招标平台调用所传该参数值，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "iiiii", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("此投标文件不是用最新的澄清文件制作"));

        //数据均匹配调用成功
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "iiiii", "useZBFileGuidNew", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));


        //招标平台调用传值userIdentifier_C，文件平台调用CAType传234校验失败，传值127校验成功
        orderNo = tapCommonFunc.initProject();
        tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "",
                "aaa@@bbb@@cccc@@ddddd@eeeeee", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "234", "",
                "", "aaa", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("制作此投标文件的电子营业执照证书未在本系统中激活，请仔细检查生成投标文件时所用证书是否正确，并重新生成"));

        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "aaa", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //招标平台调用更新userIdentifier_B和useZBFileGuid数据
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "",
                "123abc数据@@111@@222@@333", "", "useZBFileGuidNew", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //文件平台调用userIdentifier传参不等于userIdentifier_C其一，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "aaa", "useZBFileGuidNew", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("制作此投标文件的证书未在本系统中激活"));

        //文件平台调用useZBFileGuid不匹配招标平台调用所传该参数值，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "123abc数据", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
        assertEquals(true, response.contains("此投标文件不是用最新的澄清文件制作"));

        //数据均匹配调用成功
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "123abc数据", "useZBFileGuidNew", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //招标平台调用更新userIdentifier_B数据
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "", "fff@@gggg@@hhh@@iiiii",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //文件平台调用userIdentifier不匹配招标平台更新后该参数值，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdA, "1", "1", "127", "",
                "", "123abc数据", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //项目或者投标人不存在，校验失败
        response = tap.tapTenderVerify(orderNo, recordIdB, "1", "1", "127", "",
                "", "fff", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        response = tap.tapTenderVerify(orderNo + "123", recordIdA, "1", "1", "127", "",
                "", "fff", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));
    }

    /**
     * 异常流程测试-撤销投标接口
     * data数据非{\"unitname\":\"T02\",\"revoketime\":\"20211209162920\"}格式，请求接口失败
     * data数据中投标人unitname不存在链上，接口请求失败
     */
    @Test
    public void tapTenderRevokeTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();
        String response = tap.tapTenderRevoke("123", orderNo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
//        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        response = tap.tapTenderRevoke("{\"unitname\":\""+recordIdA+"\",\"revoketime\":\"20211209162920\"}", orderNo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("unitname is non-exitent"));
    }

    //    @Test
    public void InitTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();
        log.info(orderNo);
        log.info(URLEncoder.encode(URLEncoder.encode(orderNo)));

//        tap.tapProjectUpdate(orderNo, "", "",
//                "", "", "", "",
//                "5", "", 0, "",
//                "", "", null);


    }


}